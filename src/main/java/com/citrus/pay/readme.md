# 1. module定義
這個module主要用途是：連動支付系統, 一次性支付

# 2. 核心邏輯：
核心邏輯：
    放款執行 (Payout)：呼叫 PG 放款 API (Nodal Account)。
    還款連結 (Collection)：生成還款連結 (Payment Link / UPI Intent)。
    Webhook 處理：
        接收 PG 回調，去重 (Idempotency)。
        將雜亂的銀行狀態轉譯為標準事件 PAYMENT_RECEIVED。
        發送 MQ 給 Ledger。

---

# 3. 需求文件

## 3.1 這個模組在做什麼？(業務背景)

這是貸款系統的**出納**，負責兩件事：
1. **放款** — 把錢打到用戶銀行帳戶
2. **收款** — 收用戶還款的錢

所有錢的進出都經過這個模組，但它**不管帳**（記帳是 ledger 的事）。

**白話說：** 這是貸款系統的「銀行櫃台」。

### 為什麼需要這個模組？

1. **銀行 API 複雜** — 每家銀行/PG 的 API 格式都不同，需要統一封裝
2. **Webhook 去重** — 銀行可能重複發回調，需要處理冪等
3. **狀態追蹤** — 轉帳可能失敗、延遲，需要追蹤狀態

---

## 3.2 主要功能清單

| # | 功能名稱 | 說明 | 優先級 |
|---|---------|------|-------|
| 1 | **放款執行** | 呼叫 PG API 把錢打到用戶帳戶 | P0 |
| 2 | **放款狀態查詢** | 查詢放款是否成功入帳 | P0 |
| 3 | **還款連結生成** | 生成用戶還款用的連結/QR Code | P0 |
| 4 | **Webhook 接收** | 接收 PG 的付款通知回調 | P0 |
| 5 | **冪等處理** | 確保同一筆付款不會重複處理 | P0 |
| 6 | **事件發布** | 付款完成後發 MQ 給 ledger/knockoff | P1 |
| 7 | **對帳機制** | 定期和 PG 核對交易紀錄 | P2 |

---

## 3.3 業務場景說明

### 場景 1：放款流程
```
用戶完成簽約
    ↓
loancore 呼叫 pay 發起放款
    ↓
pay 準備放款請求：
    - 用戶帳號：1234567890
    - IFSC：SBIN0001234
    - 金額：₹48,000（扣掉處理費）
    ↓
pay 呼叫 PG API (Razorpay Payout)
    ↓
PG 回傳 transaction_id
    ↓
pay 等待 Webhook 確認入帳
    ↓
收到 Webhook：PROCESSED
    ↓
pay 發送 MQ 給 ledger 記帳
    ↓
通知 loancore：放款完成
```

### 場景 2：還款流程
```
用戶想還款
    ↓
App 呼叫 pay 生成還款連結
    ↓
pay 回傳：
    - Payment Link: https://rzp.io/xxx
    - UPI Intent: upi://pay?...
    - QR Code: (圖片)
    ↓
用戶點擊連結或掃碼付款
    ↓
PG 發送 Webhook 到 pay
    ↓
pay 驗證 Webhook 簽名
    ↓
pay 檢查冪等：這筆付款處理過嗎？
    - 沒有 → 繼續處理
    - 有了 → 直接回傳 200 OK，不重複處理
    ↓
pay 發送 MQ 給 knockoff（沖銷）
```

### 場景 3：放款失敗
```
pay 發起放款請求
    ↓
PG 回傳失敗：
    - BENEFICIARY_BANK_DOWN (銀行維護中)
    - INVALID_ACCOUNT (帳號無效)
    - INSUFFICIENT_BALANCE (Nodal 帳戶餘額不足)
    ↓
pay 記錄失敗原因
    ↓
通知 loancore：放款失敗
    ↓
loancore 執行補償邏輯
```

### 場景 4：自動扣款 (e-NACH)
```
還款日到了（每月 5 號）
    ↓
loancron 通知 pay：執行自動扣款
    ↓
pay 呼叫 e-NACH API 扣款
    ↓
等待 Webhook 確認
    ↓
成功 → 走正常還款流程
失敗 → 通知用戶手動還款
```

---

## 3.4 交易狀態定義

| 狀態 | 說明 | 下一步 |
|-----|------|-------|
| INITIATED | 交易已建立 | PROCESSING |
| PROCESSING | PG 處理中 | SUCCESS, FAILED |
| SUCCESS | 交易成功 | - |
| FAILED | 交易失敗 | RETRY (可選) |
| CANCELLED | 交易取消 | - |
| REFUNDED | 已退款 | - |

---

## 3.5 建議 API 清單

| API | 用途 | 呼叫者 | 備註 |
|-----|------|-------|------|
| `POST /pay/payout` | 發起放款 | loancore | |
| `GET /pay/payout/{txnId}` | 查詢放款狀態 | loancore | |
| `POST /pay/payment-link` | 生成還款連結 | App | |
| `POST /pay/webhook/{provider}` | 接收 PG Webhook | PG (外部) | 公開 endpoint |
| `POST /pay/enach/debit` | 執行 e-NACH 扣款 | loancron | |
| `GET /pay/transactions/{orderId}` | 查詢訂單所有交易 | 後台 | |

---

## 3.6 大概會怎麼開發

### Step 1：PG 封裝層
- 為每個 PG 寫一個 Adapter（RazorpayAdapter, CashfreeAdapter）
- 統一輸入輸出格式
- 實作重試機制

### Step 2：交易管理
- PaymentTransaction 表：記錄每筆交易
- 狀態機管理交易狀態

### Step 3：Webhook 處理
- 驗證簽名
- 冪等性檢查（用 transaction_id 當 key）
- 轉換為標準事件

### Step 4：MQ 發布
- 放款成功 → DISBURSEMENT_COMPLETED
- 還款成功 → PAYMENT_RECEIVED
- 發給 ledger 和 knockoff

---

## 3.7 外部依賴 (第三方服務)

| 功能 | 常見供應商 | POC 處理方式 |
|-----|-----------|-------------|
| 放款 (Payout) | Razorpay, Cashfree, PayU | Mock：延遲 2 秒回傳成功 |
| 收款 (Collection) | Razorpay, Cashfree, PayU | Mock：手動觸發 Webhook |
| e-NACH | Razorpay Mandate, NPCI | Mock：直接回傳成功 |
| UPI | NPCI, PG SDK | Mock：跳過 |

---

## 3.8 和其他模組的關係

```
            ┌─────────────┐
            │    sign     │
            └──────┬──────┘
                   │ 簽約完成
                   ▼
            ┌─────────────┐           ┌─────────────┐
            │     pay     │──────────▶│   ledger    │
            └──────┬──────┘   MQ      └─────────────┘
                   │
                   ▼
            ┌─────────────┐
            │  knockoff   │ (還款時)
            └─────────────┘
```

**通訊建議：**
- `loancore → pay`：**同步呼叫**（放款）
- `pay → ledger`：**MQ（強順序）**（必須保證入帳）
- `pay → knockoff`：**MQ**（還款沖銷）

---

## 3.9 微服務切分點 (未來考量)

**強烈建議獨立**，原因：
1. 金流是最關鍵路徑，需要最高可用性
2. 需要獨立的安全措施（PCI DSS 等）
3. 可以獨立水平擴展處理高併發

---

## 3.10 重要設計考量

| 項目 | 建議 |
|-----|------|
| **冪等性** | 用 PG 的 transaction_id 當冪等 key |
| **Webhook 驗證** | 必須驗證簽名，避免偽造 |
| **Nodal Account** | 印度規定貸款公司要有監管帳戶 |
| **對帳** | 每日和 PG 對帳，確保金額一致 |
| **異步處理** | Webhook 收到後先存 DB，再異步處理 |
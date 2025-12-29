# 1. module定義
這個module主要用途是：原先貸超的access and credit, for合作商的

# 2. 核心邏輯
核心邏輯：
    瀑布流路由 (Waterfall)：依利潤高低排序，
    依序詢問 Partner A -> Partner B。
    狀態追蹤：記錄導流成功/失敗狀態 (Lead Status)。
    回調處理：接收 Partner 的核准/放款回調 (Postback)，計算導流佣金。

---

# 3. 需求文件

## 3.1 這個模組在做什麼？(業務背景)

有些貸款申請，我們自己不想做（風險太高或不符合政策），但可以轉介給合作的金融機構。
他們如果成功放款，我們可以收一筆**導流費**。

**白話說：** 這是貸款系統的「轉介部門」，把自己不做的案子賣給別人。

### 為什麼需要這個模組？

1. **流量變現** — 拒絕的案子也有價值，轉介出去可以賺佣金
2. **用戶體驗** — 與其直接拒絕用戶，不如幫他找到其他貸款機會
3. **多元收入** — 除了利息收入，還有導流佣金收入

### 這個模組名稱由來

LSP = Loan Service Provider，或可理解為 Lead Selling Platform。  
「Access and Credit」通常指的是貸超（貸款超市）的定位——提供多家金融機構的貸款產品。

---

## 3.2 主要功能清單

| # | 功能名稱 | 說明 | 優先級 |
|---|---------|------|-------|
| 1 | **Lead 路由** | 決定導給哪個合作商 | P0 |
| 2 | **Lead 推送** | 把用戶資料發給合作商 | P0 |
| 3 | **Postback 處理** | 接收合作商的結果回調 | P0 |
| 4 | **佣金計算** | 根據成功放款計算佣金 | P1 |
| 5 | **合作商管理** | 管理合作商配置（API、分潤比例） | P1 |
| 6 | **績效報表** | 追蹤導流成功率、佣金收入 | P2 |

---

## 3.3 業務場景說明

### 場景 1：正常導流流程
```
用戶申請貸款
    ↓
origin 審核：CIBIL 650，我們的政策要求 680+
    ↓
origin 決定：不自己做，轉給 LSP
    ↓
lsp 收到 Lead，開始瀑布流路由：
    1. Partner A：要求 CIBIL > 600 ✓
    2. Partner B：要求 CIBIL > 550 ✓
    3. Partner C：只要有 PAN 就收 ✓
    ↓
選擇佣金最高的 Partner A
    ↓
推送 Lead 給 Partner A
    ↓
等待 Partner A 回調...
```

### 場景 2：合作商核准並放款
```
推送 Lead 給 Partner A
    ↓
Partner A 收到後自己做審核
    ↓
Partner A 核准！回調我們：
    - 狀態：APPROVED
    - 核准額度：₹30,000
    ↓
Partner A 對用戶放款
    ↓
Partner A 回調我們：
    - 狀態：DISBURSED
    - 放款金額：₹30,000
    ↓
lsp 計算佣金：
    - 佣金率：3%
    - 佣金 = ₹30,000 × 3% = ₹900
    ↓
記錄佣金待結算
```

### 場景 3：瀑布流 - 第一家拒絕
```
推送 Lead 給 Partner A
    ↓
Partner A 回調：REJECTED（理由：薪資太低）
    ↓
lsp 嘗試下一家：Partner B
    ↓
推送 Lead 給 Partner B
    ↓
Partner B 回調：APPROVED
    ↓
繼續後續流程...
```

### 場景 4：全部拒絕
```
嘗試了 Partner A, B, C，全部 REJECTED
    ↓
lsp 標記 Lead 狀態：EXHAUSTED
    ↓
通知 loancore：導流失敗
    ↓
訂單最終狀態：REJECTED
```

---

## 3.4 Lead 狀態定義

| 狀態 | 說明 |
|-----|------|
| PENDING | 待推送 |
| SENT | 已推送給合作商 |
| APPROVED | 合作商核准 |
| REJECTED | 合作商拒絕 |
| DISBURSED | 合作商已放款 |
| EXHAUSTED | 所有合作商都拒絕 |
| EXPIRED | 超時未回調 |

---

## 3.5 瀑布流路由邏輯

```
1. 取得符合條件的合作商列表
       │
       ▼
2. 過濾：用戶資料是否符合合作商的基本要求
       │
       ▼
3. 排序：按佣金率從高到低排序
       │
       ▼
4. 依序推送：
       - 推送給第一家
       - 等待回調（設超時）
       - 被拒絕 → 推送給下一家
       - 核准 → 結束路由
       │
       ▼
5. 全部拒絕 → EXHAUSTED
```

---

## 3.6 建議 API 清單

| API | 用途 | 呼叫者 | 備註 |
|-----|------|-------|------|
| `POST /lsp/lead/submit` | 提交 Lead | loancore / origin | |
| `GET /lsp/lead/{leadId}` | 查詢 Lead 狀態 | 後台 | |
| `POST /lsp/postback/{partnerId}` | 接收合作商回調 | Partner (外部) | 公開 |
| `GET /lsp/partner/list` | 查詢合作商列表 | 後台 | |
| `POST /lsp/partner/create` | 新增合作商 | 後台 | |
| `PUT /lsp/partner/{id}` | 更新合作商配置 | 後台 | |
| `GET /lsp/commission/pending` | 待結算佣金 | 財務 | |
| `POST /lsp/commission/settle` | 標記佣金已結算 | 財務 | |

---

## 3.7 大概會怎麼開發

### Step 1：合作商管理
- Partner 表：存合作商資訊
- 包含 API 設定、佣金率、准入條件

### Step 2：Lead 管理
- Lead 表：存導流記錄
- 關聯到原始的 LoanOrder

### Step 3：路由邏輯
- Waterfall 引擎
- 支援條件過濾和排序

### Step 4：推送 API
- 為每個合作商寫 Adapter
- 統一請求/回應格式

### Step 5：Postback 處理
- 接收回調，更新 Lead 狀態
- 計算佣金

---

## 3.8 和其他模組的關係

```
            ┌─────────────┐
            │   origin    │
            └──────┬──────┘
                   │ 導流請求
                   ▼
            ┌─────────────┐           ┌─────────────────┐
            │     lsp     │◀─────────▶│    Partners     │
            └──────┬──────┘  API/回調  │ (外部合作商)    │
                   │                  └─────────────────┘
                   │ 結果通知
                   ▼
            ┌─────────────┐
            │  loancore   │
            └─────────────┘
```

**通訊建議：**
- `origin → lsp`：透過 **loancore 協調**
- `lsp → Partners`：**同步 HTTP**
- `Partners → lsp`：**Postback (Webhook)**
- `lsp → loancore`：**MQ** (結果通知)

---

## 3.9 微服務切分點 (未來考量)

**建議獨立**，原因：
1. 和多個外部系統對接，需要獨立管理連線
2. 佣金計算是獨立的業務邏輯
3. 可以獨立擴展應對高流量

---

## 3.10 合作商對接範例

### Partner API 規格（推送 Lead）

**Request:**
```json
{
  "lead_id": "LEAD-001",
  "customer": {
    "name": "Ram Kumar",
    "pan": "ABCDE1234F",
    "mobile": "9876543210",
    "email": "ram@example.com"
  },
  "loan_request": {
    "amount": 50000,
    "tenure": 12,
    "purpose": "personal"
  },
  "bureau": {
    "cibil_score": 650,
    "foir": 45
  }
}
```

**Response (Sync):**
```json
{
  "partner_lead_id": "P-12345",
  "status": "RECEIVED"
}
```

### Postback 規格（合作商回調）

```json
{
  "lead_id": "LEAD-001",
  "partner_lead_id": "P-12345",
  "status": "DISBURSED",
  "disbursed_amount": 30000,
  "disbursed_date": "2024-01-15",
  "signature": "abc123..."  // 驗證用
}
```

---

## 3.11 重要設計考量

| 項目 | 建議 |
|-----|------|
| **超時處理** | 合作商不回調時，設定超時自動嘗試下一家 |
| **簽名驗證** | Postback 要驗證簽名，避免偽造 |
| **去重** | 同一 Postback 可能重複發送，要冪等處理 |
| **佣金對帳** | 定期和合作商對帳佣金 |
| **API 版本** | 不同合作商 API 格式不同，要有 Adapter 層 |
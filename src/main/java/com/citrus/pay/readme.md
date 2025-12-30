# Pay Module

這是貸款系統的**支付模組**，負責放款和收款。

---

# 1. 模組定義

| 項目 | 說明 |
|------|------|
| 角色 | 銀行櫃台 |
| 對外 API | ⚠️ Webhook 需公開 |
| 核心職責 | 放款、收款、Webhook 處理 |

---

# 2. 核心流程

### 放款
```
loancore (SIGNED)
    │
    │ POST /pay/payout
    ▼
┌─────────────────┐
│       pay       │ → 呼叫 PG API
└────────┬────────┘
         │
         ▼ Webhook 確認
┌─────────────────┐
│     ledger      │ ← 記帳
└─────────────────┘
```

### 收款
```
用戶還款
    │
    ▼
PG Webhook → pay → knockoff → ledger
```

---

# 3. 主要功能

| # | 功能 | 說明 | 優先級 |
|---|------|------|-------|
| 1 | **放款** | 呼叫 PG 打款 | P0 |
| 2 | **收款** | 生成還款連結 | P0 |
| 3 | **Webhook** | 處理 PG 回調 | P0 |
| 4 | **冪等** | 防止重複處理 | P0 |
| 5 | **e-NACH 扣款** | 自動扣款 | P1 |

---

# 4. API 清單

### Phase 1

| API | 用途 | 呼叫者 |
|-----|------|-------|
| `POST /pay/payout` | 發起放款 | loancore |
| `POST /pay/payout/{txnId}` | 查詢放款狀態 | loancore |
| `POST /pay/webhook/{provider}` | 接收 PG 回調 | PG (外部) |

### Phase 2

| API | 用途 | 呼叫者 |
|-----|------|-------|
| `POST /pay/payment-link` | 生成還款連結 | App |
| `POST /pay/enach/debit` | e-NACH 扣款 | loancron |
| `POST /pay/transactions/{orderId}` | 查詢交易記錄 | 後台 |

---

# 5. 外部依賴

| 功能 | 供應商 | POC 處理 |
|-----|-------|---------|
| 放款 | Razorpay, Cashfree | Mock |
| 收款 | Razorpay | Mock |
| e-NACH | Razorpay Mandate | Mock |

---

# 6. 設計考量

| 項目 | 建議 |
|-----|------|
| **冪等** | 用 txnId 去重 |
| **簽名驗證** | Webhook 必須驗簽 |
| **對帳** | 每日和 PG 對帳 |
# Origin Module

這是貸款系統的**對外入口**，負責接收用戶申請、決策審核。

> [!IMPORTANT]
> **origin 不存儲申請資料**
> - 收到申請後**立刻**在 loancore 建單 (status=PENDING)
> - origin 只負責：接收請求、決策邏輯、額度計算、利率定價
> - 所有狀態追蹤統一在 `loancore.LoanOrder`

---

# 1. 模組定義

| 項目 | 說明 |
|------|------|
| 角色 | 審核官 + 定價師 + **對外入口** |
| 對外 API | ✅ 接收前端請求 |
| 核心職責 | 接收申請 → **立刻建單** → 決策引擎、額度計算、利率定價 |
| 資料存儲 | ❌ **不存資料**，統一由 loancore 管理 |

---

# 2. 核心流程 (Saga 模式)

#### 階段一：用戶申請 (同步)

```
前端 App
    │
    │ POST /origin/apply
    ▼
┌──────────────┐         ┌──────────────┐
│    origin    │────────▶│   loancore   │ 建單 status=PENDING
│ 1. 接收申請  │         └──────────────┘
│ 2. 呼叫建單  │
│ 3. 返回 ID   │
└──────────────┘
```

#### 階段二：排程審核 (異步，由 loancron 觸發)

```
┌──────────────┐
│   loancore   │ 1. 查詢 PENDING 訂單
│              │ 2. 呼叫 bureau
└──────┬───────┘
       │
┌──────▼───────┐
│    bureau    │ 收集資料 (CIBIL, BSA...)
└──────┬───────┘
       │
┌──────▼───────┐
│   loancore   │ 3. 更新 BUREAU_CHECK
│              │ 4. 呼叫 origin
└──────┬───────┘
       │
┌──────▼───────┐
│    origin    │ ← 決策引擎 (回傳結果)
│ - 通過 → OFFER_READY + 額度/利率
│ - 拒絕 → REJECTED
│ - 導流 → LSP_ROUTING
└──────┬───────┘
       │
┌──────▼───────┐
│   loancore   │ 5. 更新狀態
└──────────────┘
```

---

# 3. 主要功能

| # | 功能 | 說明 | 優先級 |
|---|------|------|-------|
| 1 | **接收申請** | 前端入口，**立刻呼叫 loancore 建單** | P0 |
| 2 | **Dedupe 去重** | 同一人短期內重複申請擋掉 (查 loancore) | P0 |
| 3 | **黑名單過濾** | 在黑名單的人直接拒絕 | P0 |
| 4 | **決策引擎** | 根據徵信資料決定 Pass/Reject | P0 |
| 5 | **額度計算** | 根據收入、信用分數計算可貸額度 | P0 |
| 6 | **利率定價** | 根據風險等級決定利率 | P0 |
| 7 | **決策結果回傳** | 將決策結果回傳給 loancore 更新狀態 | P0 |

---

# 4. API 清單

### Phase 1

| API | 用途 | 呼叫者 |
|-----|------|-------|
| `POST /origin/apply` | 用戶申請貸款 → **立刻建單** | 前端 App |

> [!NOTE]
> 查詢申請狀態改為呼叫 `loancore` 的 API (`/loancore/order/{id}`)

### Phase 2

| API | 用途 | 呼叫者 |
|-----|------|-------|
| `POST /origin/evaluate` | 執行決策評估 | loancore (內部) |
| `POST /origin/offer/{orderId}` | 查詢核准 Offer | sign, App |
| `POST /origin/blacklist/add` | 新增黑名單 | 後台 |
| `POST /origin/blacklist/remove` | 移除黑名單 | 後台 |
| `POST /origin/blacklist/check` | 檢查是否在黑名單 | origin (內部) |
| `POST /origin/decision/{orderId}` | 查詢決策詳情 | 後台, loancore |

---

# 5. 資料模型

> [!IMPORTANT]
> **origin 不存儲 LoanApplication**
> 
> 所有申請資料統一存在 `loancore.LoanOrder`，狀態流程為：
> ```
> PENDING → BUREAU_CHECK → UNDERWRITING → OFFER_READY → ...
>                                       → REJECTED (終態)
>                                       → LSP_ROUTING (終態)
> ```

### origin 存儲的輔助資料

| 表 | 用途 | 優先級 |
|---|------|-------|
| **Blacklist** | 黑名單 | P0 |
| **DecisionAudit** | 決策審計軌跡 | P0 |
| DecisionRule | 決策規則配置 | P1 (可先 hardcode) |
| OfferTemplate | Offer 模板 | P2 |

---

### Blacklist (P0)

黑名單表，用於快速拒絕禁止貸款的用戶。

```sql
Blacklist (
    blacklistId VARCHAR(64) PRIMARY KEY,
    identifierType VARCHAR(20) NOT NULL,  -- PAN / AADHAAR / MOBILE
    identifierValue VARCHAR(100) NOT NULL,
    reason VARCHAR(500),                  -- 原因
    addedBy VARCHAR(100),                 -- 誰加入
    createdAt TIMESTAMP NOT NULL,
    expiresAt TIMESTAMP,                  -- 可選，永久拉黑則為 NULL
    INDEX idx_identifier (identifierType, identifierValue)
)
```

**欄位說明**：
- `identifierType`：黑名單類型 (PAN / AADHAAR / MOBILE)
- `identifierValue`：對應的值
- `expiresAt`：過期時間，NULL 表示永久拉黑

---

### DecisionAudit (P0)

決策審計軌跡，記錄每次決策的詳細資訊。

```sql
DecisionAudit (
    decisionAuditId VARCHAR(64) PRIMARY KEY,
    loanOrderId VARCHAR(64) NOT NULL,
    decisionResult VARCHAR(20) NOT NULL,  -- APPROVED / REJECTED / LSP_ROUTING
    
    -- 核准資訊 (APPROVED 時才有值)
    approvedAmount DECIMAL(15,2),
    approvedRate DECIMAL(5,2),
    approvedTenure INTEGER,
    
    -- 拒絕資訊 (REJECTED 時才有值)
    rejectReason VARCHAR(50),             -- CIBIL_LOW / FOIR_HIGH / BLACKLIST etc.
    
    -- 決策細節
    ruleName VARCHAR(100),                -- 觸發的規則名稱
    inputSnapshot TEXT,                   -- 輸入資料快照 (JSON)
    
    createdAt TIMESTAMP NOT NULL,
    INDEX idx_order (loanOrderId)
)
```

**欄位說明**：
- `decisionResult`：決策結果
  - `APPROVED`：核准
  - `REJECTED`：拒絕
  - `LSP_ROUTING`：導流給合作商
- `approvedAmount`：核准額度 (僅 APPROVED)
- `approvedRate`：核准利率 (僅 APPROVED)
- `rejectReason`：拒絕原因代碼 (僅 REJECTED)
- `ruleName`：觸發的規則名稱 (例如 "CIBIL_THRESHOLD_CHECK")
- `inputSnapshot`：輸入資料快照，JSON 格式，例如：
  ```json
  {
    "cibil_score": 650,
    "monthly_income": 30000,
    "existing_loans": 2,
    "foir": 0.45
  }
  ```

**查詢範例**：
```sql
-- 查詢訂單的決策詳情
SELECT * FROM DecisionAudit WHERE loanOrderId = ?

-- 統計拒絕原因分布
SELECT rejectReason, COUNT(*) 
FROM DecisionAudit 
WHERE decisionResult = 'REJECTED' 
GROUP BY rejectReason
```

---

# 6. 決策引擎規則 (範例)

### 硬拒規則

| 規則 | 條件 | 說明 |
|-----|------|------|
| R001 | CIBIL < 500 | 信用分太低 |
| R002 | 有呆帳 > ₹50,000 | 有大額呆帳 |
| R003 | 近 6 個月新增貸款 > 3 筆 | 疑似貸款堆疊 |
| R004 | 在黑名單中 | 直接拒絕 |

### 額度計算公式

```
核准額度 = 月收入 × (50% - FOIR) × 期數

例：月收入 = ₹50,000，FOIR = 30%，期數 = 12
額度 = 50,000 × (50% - 30%) × 12 = ₹120,000
```

---

# 7. 和其他模組的關係

```
            ┌─────────────┐
            │   前端 App   │
            └──────┬──────┘
                   │ POST /origin/apply
                   ▼
            ┌─────────────┐     ┌─────────────┐
            │   origin    │────▶│  loancore   │ ← 立刻建單
            │ (不存資料)  │     │ (LoanOrder) │
            └─────────────┘     └──────┬──────┘
                                       │
                          ┌────────────┼────────────┐
                          ▼            ▼            ▼
                    ┌────────┐  ┌────────┐  ┌────────┐
                    │bureau  │  │ origin │  │  lsp   │
                    │(徵信)  │  │(決策)  │  │(導流)  │
                    └────────┘  └────────┘  └────────┘
```

**通訊方式：**
- 前端 → origin：**同步 HTTP**
- origin → loancore：**同步 HTTP**（立刻建單）
- loancore → origin：**同步 HTTP**（請求決策）
- origin → loancore：**同步 HTTP**（回傳決策結果）

---

# 8. 設計考量

| 項目 | 建議 |
|-----|------|
| **去重策略** | 同一 PAN 30 天內不重複處理 (查詢 loancore) |
| **申請限流** | 防止惡意刷單 |
| **決策可配置** | 規則可動態調整 |
| **審計軌跡** | 每次決策記錄原因 (存在 LoanOrderHistory) |
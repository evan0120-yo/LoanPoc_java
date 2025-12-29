# 1. module定義
這個module主要用途是：總狀態機+saga

# 2. 核心邏輯
核心邏輯：
    訂單狀態管理：管理貸款訂單從申請到結案的完整生命週期
    Saga 協調：協調跨模組的分散式交易，處理補償邏輯
    事件發布：發布狀態變更事件給其他模組

---

# 3. 需求文件

## 3.1 這個模組在做什麼？(業務背景)

這是整個貸款系統的**指揮中心**。  
所有貸款訂單的狀態都由這個模組管理，它負責協調其他模組的執行順序。

**白話說：** 如果貸款系統是一家公司，這個模組就是「總經理」，負責安排誰先做、誰後做。

### 為什麼需要這個模組？

1. **狀態統一管理** — 一筆貸款會經過 10+ 個狀態，需要有人統一紀錄
2. **Saga 模式** — 跨模組操作如果失敗，需要有人負責「回滾」或「補償」
3. **事件溯源** — 所有狀態變更都要可追溯，方便 debug 和審計

---

## 3.2 主要功能清單

| # | 功能名稱 | 說明 | 優先級 |
|---|---------|------|-------|
| 1 | **建立貸款訂單** | 用戶申請時建立訂單，初始狀態 PENDING | P0 |
| 2 | **狀態機管理** | 管理訂單狀態轉換，確保只能按規則轉換 | P0 |
| 3 | **Saga 協調器** | 協調跨模組流程，處理失敗補償 | P1 |
| 4 | **狀態變更事件** | 狀態變更時發布事件，通知其他模組 | P2 |
| 5 | **訂單查詢** | 提供訂單詳情查詢，給 App 和後台用 | P0 |
| 6 | **訂單歷程記錄** | 記錄每次狀態變更的時間和原因（粗粒度，用於找故事） | P1 |

---

## 3.3 業務場景說明

### 場景 1：正常貸款申請流程 (Happy Path)
```
用戶申請
    ↓
loancore 建立訂單 (PENDING)
    ↓
loancore 呼叫 bureau → 狀態變 BUREAU_CHECK
    ↓
bureau 完成 → 狀態變 UNDERWRITING
    ↓
origin 核准 → 狀態變 OFFER_READY
    ↓
loancore 呼叫 sign → 狀態變 SIGN_PENDING
    ↓
sign 完成 → 狀態變 SIGNED
    ↓
loancore 呼叫 pay → 狀態變 DISBURSAL_PENDING
    ↓
pay 放款成功 → 狀態變 DISBURSED → ACTIVE
```

### 場景 2：Saga 補償（放款失敗）
```
訂單狀態已經是 SIGNED
    ↓
loancore 呼叫 pay 放款
    ↓
pay 放款失敗（銀行帳戶有問題）
    ↓
loancore 執行補償：
    1. 通知用戶放款失敗
    2. 狀態變 DISBURSAL_FAILED
    3. 記錄失敗原因
    4. (可選) 自動重試或等用戶修改帳戶
```

### 場景 3：查詢訂單狀態
```
用戶打開 App 查看貸款進度
    ↓
App 呼叫 loancore 查詢
    ↓
loancore 回傳：當前狀態 + 預計下一步 + 歷史軌跡
```

---

## 3.4 訂單狀態定義

| 狀態 | 中文 | 說明 | 下一步可能狀態 |
|-----|------|------|--------------| 
| PENDING | 待處理 | 訂單剛建立 | BUREAU_CHECK |
| BUREAU_CHECK | 徵信查詢中 | 正在查徵信 | UNDERWRITING, REJECTED |
| UNDERWRITING | 審核中 | 決策引擎處理中 | OFFER_READY, REJECTED, LSP_ROUTING |
| REJECTED | 已拒絕 | 終態 | - |
| LSP_ROUTING | 轉導合作商 | 終態 | - |
| OFFER_READY | Offer已產生 | 等待用戶簽約 | SIGN_PENDING |
| SIGN_PENDING | 待簽約 | 用戶簽約中 | SIGNED, CANCELLED |
| SIGNED | 已簽約 | 等待放款 | DISBURSAL_PENDING |
| DISBURSAL_PENDING | 待放款 | 正在轉帳 | DISBURSED, DISBURSAL_FAILED |
| DISBURSAL_FAILED | 放款失敗 | 需要處理 | DISBURSAL_PENDING (重試) |
| DISBURSED | 已放款 | 錢已到帳 | ACTIVE |
| ACTIVE | 還款中 | 正常還款期 | OVERDUE, CLOSED |
| OVERDUE | 逾期 | 有未還款項 | ACTIVE, NPA, CLOSED |
| NPA | 不良資產 | 逾期 > 90 天 | WRITTEN_OFF, CLOSED |
| CLOSED | 已結清 | 終態 | - |
| WRITTEN_OFF | 已呆帳核銷 | 終態 | - |
| CANCELLED | 已取消 | 終態 | - |

### 狀態流程圖

```
                              ┌──────────────┐
                              │   REJECTED   │ (終態)
                              └──────────────┘
                                    ↑
    ┌─────────┐    ┌──────────────┐ │ ┌──────────────┐
    │ PENDING │───→│ BUREAU_CHECK │─┼→│ UNDERWRITING │
    └─────────┘    └──────────────┘ │ └──────┬───────┘
                                    │        │
                              ┌─────┴────┐   ├───────────────────┐
                              │LSP_ROUTING│   │                   │
                              └──────────┘   ↓                   ↓
                              (終態)   ┌───────────┐      ┌──────────┐
                                       │OFFER_READY│      │ REJECTED │
                                       └─────┬─────┘      └──────────┘
                                             │
                                             ↓
                                       ┌────────────┐     ┌───────────┐
                                       │SIGN_PENDING│────→│ CANCELLED │ (終態)
                                       └─────┬──────┘     └───────────┘
                                             │
                                             ↓
                                       ┌──────────┐
                                       │  SIGNED  │
                                       └────┬─────┘
                                            │
                                            ↓
    ┌────────────────┐  ←──(重試)──  ┌──────────────────┐
    │DISBURSAL_FAILED│──────────────→│ DISBURSAL_PENDING│
    └────────────────┘               └────────┬─────────┘
                                              │
                                              ↓
                                       ┌───────────┐
                                       │ DISBURSED │
                                       └─────┬─────┘
                                             │
                                             ↓
              ┌────────┐  ←──(還清)──  ┌──────────┐
              │ ACTIVE │←─────────────│  ACTIVE  │
              └───┬────┘              └────┬─────┘
                  │                        │
          逾期    ↓                        ↓ 結清
              ┌─────────┐            ┌──────────┐
              │ OVERDUE │───────────→│  CLOSED  │ (終態)
              └────┬────┘            └──────────┘
                   │
          90+天    ↓
              ┌─────────┐
              │   NPA   │
              └────┬────┘
                   │
                   ├───────────────→ CLOSED (終態)
                   ↓
              ┌─────────────┐
              │ WRITTEN_OFF │ (終態)
              └─────────────┘
```

### 可倒退/循環的狀態
| 循環 | 說明 | History 記錄價值 |
|-----|------|-----------------|
| `DISBURSAL_FAILED ↔ DISBURSAL_PENDING` | 放款失敗後重試 | 記錄每次重試的時間和失敗原因 |
| `ACTIVE ↔ OVERDUE` | 用戶逾期後還清，可能多次循環 | 記錄每次進出逾期的時間 |

---

## 3.5 建議 API 清單

| API | 用途 | 呼叫者 | POC 狀態 |
|-----|------|-------|----------|
| `POST /loancore/order/init` | 建立新訂單 | App | ✅ 已實作 |
| `POST /loancore/order/findById` | 查詢訂單詳情 | App, 後台 | ✅ 已實作 |
| `POST /loancore/order/all` | 查詢用戶所有訂單 | App | ✅ 已實作 |
| `POST /loancore/order/updateState` | 更新訂單狀態 | 各模組 | ✅ 已實作 |
| `GET /loan/{orderId}/history` | 查詢狀態歷程 | App, 後台 | ⏸️ 待實作 (需 LoanOrderHistory 表) |
| `POST /loan/{orderId}/cancel` | 用戶取消申請 | App | ⏸️ 可選 |
| `POST /loan/{orderId}/retry` | 重試失敗步驟 | 後台 | ⏸️ 可選 |

---

## 3.6 資料表設計（混合式架構）

### 設計理念
採用「混合式」架構：loancore 存粗粒度歷史（找故事用），各子模組存細粒度業務記錄。

### loancore 的表
```sql
-- 總狀態機（當前狀態）
LoanOrder (
    loanOrderId,
    userId,
    loanState,      -- 當前狀態
    amount,
    createdAt,
    updatedAt
)

-- 狀態變更歷史（粗粒度，找故事用）
LoanOrderHistory (
    id,
    loanOrderId,
    fromStatus,     -- 從什麼狀態
    toStatus,       -- 變成什麼狀態
    triggeredBy,    -- 誰觸發的 (bureau/origin/system/user)
    remark,         -- 備註
    createdAt
)
```

### 各子模組的表（細粒度）
```sql
-- bureau 模組
BureauRecord (orderId, provider, score, riskLevel, rawResponse, createdAt)
BureauApiLog (orderId, requestBody, responseBody, status, createdAt)

-- origin 模組
UnderwritingRecord (orderId, decision, approvedAmount, reason, createdAt)

-- sign 模組
SigningContract (orderId, contractUrl, signedAt, eSignRef, createdAt)

-- pay 模組
DisbursalRecord (orderId, bankRef, amount, status, createdAt)
DisbursalApiLog (orderId, requestBody, responseBody, status, createdAt)
```

### 找故事方式
- **粗粒度故事**：查 `LoanOrderHistory` 一張表即可
- **細粒度細節**：去對應子模組查 ApiLog

---

## 3.7 開發步驟

### Step 1：定義狀態機
- 用 Enum 定義所有狀態 (`LoanStateEnum`)
- 定義允許的狀態轉換規則（`LoanOrderGuardService`）

### Step 2：建立訂單 Entity
- LoanOrder 表：存訂單主資料 ✅ 已完成
- LoanOrderHistory 表：存每次狀態變更 ⏸️ 待實作

### Step 3：實作 Saga 協調器
- POC 可以用簡單的 if-else
- 正式環境可用 Saga 框架

### Step 4：事件發布
- 狀態變更時發 MQ 事件
- 其他模組可以訂閱這些事件做後續處理

---

## 3.8 和其他模組的關係

```
                    ┌─────────────┐
     ┌──────────────│  loancore   │──────────────┐
     │              │ (總指揮中心) │              │
     │              └──────┬──────┘              │
     │                     │                     │
     ▼                     ▼                     ▼
┌─────────┐          ┌─────────┐          ┌─────────┐
│ bureau  │          │  origin │          │   sign  │
└─────────┘          └─────────┘          └─────────┘
     │                     │                     │
     └──────────┬──────────┴──────────┬──────────┘
                │                     │
                ▼                     ▼
          ┌─────────┐          ┌─────────┐
          │   pay   │          │   lsp   │
          └─────────┘          └─────────┘
```

**通訊建議：**
- `loancore → 各模組`：**同步呼叫** 或 **Command MQ**
- `各模組 → loancore`：**MQ 事件通知** 狀態更新

---

## 3.9 微服務切分點 (未來考量)

**不建議獨立拆分**，原因：
1. 這是核心協調器，和所有模組都有關聯
2. 狀態一致性要求高，拆分會增加分散式事務複雜度
3. 建議保持在主服務內，但可以獨立 DB schema

---

## 3.10 重要設計考量

| 項目 | 建議 |
|-----|------|
| **狀態轉換鎖** | 同一訂單同時只能有一個狀態轉換，避免競爭 |
| **冪等性** | 狀態更新 API 要支援冪等，重複呼叫不會出錯 |
| **歷程不可變** | LoanOrderHistory 只能 INSERT，不能 UPDATE/DELETE |
| **超時處理** | 某些狀態要有超時機制（如 SIGN_PENDING 超過 7 天自動 CANCELLED） |
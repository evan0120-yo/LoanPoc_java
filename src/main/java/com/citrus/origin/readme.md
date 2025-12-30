# Origin Module

這是貸款系統的**對外入口**，負責接收用戶申請、徵信查詢、決策審核。

---

# 1. 模組定義

| 項目 | 說明 |
|------|------|
| 角色 | 審核官 + 定價師 + **對外入口** |
| 對外 API | ✅ 接收前端請求 |
| 核心職責 | 接收申請、決策引擎、額度計算、利率定價 |

---

# 2. 核心流程

```
前端 App
    │
    │ POST /origin/apply
    ▼
┌─────────────────┐
│     origin      │
│  1. 存申請記錄   │
│  2. 返回「審核中」│
└────────┬────────┘
         │
         ▼ (loancron 排程觸發)
┌─────────────────┐
│     bureau      │ ← 徵信查詢
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│     origin      │ ← 決策引擎
│  - 通過 → loancore 建單
│  - 拒絕 → 結束 or lsp 導流
└─────────────────┘
```

---

# 3. 主要功能

| # | 功能 | 說明 | 優先級 |
|---|------|------|-------|
| 1 | **接收申請** | 前端入口，存入 LoanApplication | P0 |
| 2 | **Dedupe 去重** | 同一人短期內重複申請擋掉 | P0 |
| 3 | **黑名單過濾** | 在黑名單的人直接拒絕 | P0 |
| 4 | **決策引擎** | 根據徵信資料決定 Pass/Reject | P0 |
| 5 | **額度計算** | 根據收入、信用分數計算可貸額度 | P0 |
| 6 | **利率定價** | 根據風險等級決定利率 | P0 |
| 7 | **LSP 路由** | 自己不做的案子導給合作商 | P1 |

---

# 4. API 清單

### Phase 1

| API | 用途 | 呼叫者 |
|-----|------|-------|
| `POST /origin/apply` | 用戶申請貸款 | 前端 App |
| `POST /origin/application/{id}` | 查詢申請狀態 | 前端 App |

### Phase 2

| API | 用途 | 呼叫者 |
|-----|------|-------|
| `POST /origin/evaluate` | 執行決策評估 | loancron (內部) |
| `POST /origin/offer/{orderId}` | 查詢核准 Offer | sign, App |
| `POST /origin/blacklist/add` | 新增黑名單 | 後台 |
| `POST /origin/blacklist/remove` | 移除黑名單 | 後台 |

---

# 5. 資料模型

### LoanApplication (申請記錄)

| 欄位 | 類型 | 說明 |
|-----|------|------|
| applicationId | String | 申請 ID |
| userId | String | 用戶 ID |
| mobile | String | 手機號碼 |
| panNumber | String | PAN 卡號 |
| name | String | 姓名 |
| appliedAmount | BigDecimal | 申請金額 |
| status | Enum | PENDING / EVALUATING / APPROVED / REJECTED / CONVERTED |
| loanOrderId | String | 轉換後的訂單 ID (nullable) |
| createdAt | Instant | 申請時間 |
| evaluatedAt | Instant | 評估時間 |

### 申請狀態流程

```
PENDING → EVALUATING → APPROVED → CONVERTED (建單成功)
                    → REJECTED (拒絕)
                    → LSP_ROUTING (導流)
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
            ┌─────────────┐
            │   origin    │ ← 對外入口
            └──────┬──────┘
                   │
        ┌──────────┼──────────┐
        ▼          ▼          ▼
   ┌────────┐ ┌────────┐ ┌────────┐
   │bureau  │ │loancore│ │  lsp   │
   │(徵信)  │ │(建單)  │ │(導流)  │
   └────────┘ └────────┘ └────────┘
```

**通訊方式：**
- 前端 → origin：**同步 HTTP**
- origin → bureau：**同步 HTTP**
- origin → loancore：**同步 HTTP**（建單）
- origin → lsp：**MQ**（導流）

---

# 8. 設計考量

| 項目 | 建議 |
|-----|------|
| **去重策略** | 同一 PAN 30 天內不重複處理 |
| **申請限流** | 防止惡意刷單 |
| **決策可配置** | 規則可動態調整 |
| **審計軌跡** | 每次決策記錄原因 |
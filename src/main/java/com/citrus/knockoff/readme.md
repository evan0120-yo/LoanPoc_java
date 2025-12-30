# Knockoff Module

這是貸款系統的**沖銷模組**，負責還款分配。

---

# 1. 模組定義

| 項目 | 說明 |
|------|------|
| 角色 | 分錢機 |
| 對外 API | ❌ 僅供 pay 調用 |
| 核心職責 | Waterfall 分配、溢繳處理 |

---

# 2. Waterfall 概念

```
還款 ₹5,000 進來：
    1. GST (稅)      ← 最優先
    2. Penalty (罰)
    3. Interest (息)
    4. Principal (本) ← 最後
```

---

# 3. 主要功能

| # | 功能 | 說明 | 優先級 |
|---|------|------|-------|
| 1 | **Waterfall 計算** | 按順序分配 | P0 |
| 2 | **部分還款** | 不足時按順序填 | P0 |
| 3 | **溢繳處理** | 多還的存起來 | P0 |
| 4 | **提前還款** | 計算清償金額 | P1 |

---

# 4. API 清單

### Phase 1

| API | 用途 | 呼叫者 |
|-----|------|-------|
| `POST /knockoff/calculate` | 計算分配(預覽) | pay |
| `POST /knockoff/apply` | 執行沖銷 | pay |

### Phase 2

| API | 用途 | 呼叫者 |
|-----|------|-------|
| `POST /knockoff/history/{orderId}` | 沖銷歷史 | 後台 |
| `POST /knockoff/prepay/calculate` | 提前還款計算 | App |
| `POST /knockoff/excess/refund` | 退還溢繳 | 後台 |

---

# 5. 和 Ledger 的關係

```
pay (收到還款)
    │ MQ
    ▼
knockoff (計算分配)
    │ 同步調用 (原子性)
    ▼
ledger (入帳)
```

---

# 6. 設計考量

| 項目 | 建議 |
|-----|------|
| **原子性** | knockoff + ledger 同事務 |
| **可配置** | Waterfall 順序可調 |
| **精度** | BigDecimal |
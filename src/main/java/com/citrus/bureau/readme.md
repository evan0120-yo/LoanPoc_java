# Bureau Module

這是貸款系統的**徵信模組**，負責對接第三方數據供應商。

---

# 1. 模組定義

| 項目 | 說明 |
|------|------|
| 角色 | 偵探部門 / 防腐層 |
| 對外 API | ❌ 僅供 origin 調用 |
| 核心職責 | PAN 驗證、CIBIL 查詢、BSA 分析、結果標準化 |

---

# 2. 核心流程

```
origin (需要徵信)
    │
    │ POST /bureau/report/full
    ▼
┌─────────────────┐
│     bureau      │
│  1. PAN 驗證    │
│  2. CIBIL 查詢  │
│  3. BSA 分析    │
└────────┬────────┘
         │
         ▼ 標準化報告
┌─────────────────┐
│     origin      │ ← 用報告做決策
└─────────────────┘
```

---

# 3. 主要功能

| # | 功能 | 說明 | 優先級 |
|---|------|------|-------|
| 1 | **PAN 驗證** | 驗證稅號有效性 | P0 |
| 2 | **CIBIL 查詢** | 取得信用分數 | P0 |
| 3 | **BSA 分析** | 解析銀行流水 | P0 |
| 4 | **結果快取** | 30 天內不重複查詢 | P0 |
| 5 | **綜合報告** | 打包成標準格式 | P1 |

---

# 4. API 清單

### Phase 1

| API | 用途 | 呼叫者 |
|-----|------|-------|
| `POST /bureau/report/full` | 一次做完全部徵信 | origin |
| `POST /bureau/report/{applicationId}` | 查詢既有報告 | origin |

### Phase 2

| API | 用途 | 呼叫者 |
|-----|------|-------|
| `POST /bureau/pan/verify` | 單獨驗證 PAN | origin |
| `POST /bureau/cibil/inquiry` | 單獨查 CIBIL | origin |
| `POST /bureau/bsa/analyze` | 單獨分析 BSA | origin |

---

# 5. 外部依賴

| 功能 | 供應商 | POC 處理 |
|-----|-------|---------|
| PAN 驗證 | Karza, Signzy | Mock |
| CIBIL | TransUnion | Mock |
| BSA | Perfios, AA | Mock |

---

# 6. 設計考量

| 項目 | 建議 |
|-----|------|
| **防腐層** | 統一輸出格式 |
| **快取** | 減少查詢成本 |
| **重試** | 外部 API 失敗要重試 |
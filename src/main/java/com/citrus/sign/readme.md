# Sign Module

這是貸款系統的**簽約模組**，負責法律文件生成與銀行帳戶驗證。

---

# 1. 模組定義

| 項目 | 說明 |
|------|------|
| 角色 | 法務部 |
| 對外 API | ⚠️ 部分給前端 App |
| 核心職責 | KFS 生成、Penny Drop、e-Sign、e-NACH |

---

# 2. 核心流程

```
loancore (OFFER_READY)
    │
    │ 觸發簽約
    ▼
┌─────────────────┐
│      sign       │
│  1. 生成 KFS    │
│  2. Penny Drop  │
│  3. e-Sign      │
│  4. e-NACH      │
└────────┬────────┘
         │
         ▼ 簽約完成
┌─────────────────┐
│    loancore     │ ← 更新狀態 SIGNED
└─────────────────┘
```

---

# 3. 主要功能

| # | 功能 | 說明 | 優先級 |
|---|------|------|-------|
| 1 | **KFS 生成** | 關鍵事實聲明 PDF | P0 |
| 2 | **Penny Drop** | 驗證銀行帳戶 | P0 |
| 3 | **e-Sign** | Aadhaar 電子簽章 | P0 |
| 4 | **e-NACH** | 自動扣款授權 | P1 |

---

# 4. API 清單

### Phase 1

| API | 用途 | 呼叫者 |
|-----|------|-------|
| `POST /sign/kfs/generate` | 生成 KFS | loancore |
| `POST /sign/kfs/confirm` | 確認已閱讀 | App |
| `POST /sign/penny-drop` | 驗證銀行帳戶 | loancore |

### Phase 2

| API | 用途 | 呼叫者 |
|-----|------|-------|
| `POST /sign/esign/initiate` | 發起 e-Sign | loancore |
| `POST /sign/esign/verify` | 驗證 OTP | App |
| `POST /sign/enach/setup` | 設定 e-NACH | loancore |
| `POST /sign/agreement/{orderId}` | 取得合約 | App |

---

# 5. 外部依賴

| 功能 | 供應商 | POC 處理 |
|-----|-------|---------|
| Penny Drop | Razorpay | Mock |
| e-Sign | NSDL, Digio | Mock |
| e-NACH | Razorpay Mandate | Mock |

---

# 6. 設計考量

| 項目 | 建議 |
|-----|------|
| **合規** | KFS 必須符合 RBI 規定 |
| **超時** | 簽約超時自動過期 |
| **存檔** | 合約要永久保存 |
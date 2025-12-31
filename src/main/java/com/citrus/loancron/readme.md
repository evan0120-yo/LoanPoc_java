# Loancron Module

這是貸款系統的**排程器**，負責定時觸發各種任務。

---

# 1. 模組定義

| 項目 | 說明 |
|------|------|
| 角色 | 值夜班的人 / 排程觸發器 |
| 對外 API | ❌ 僅供後台測試用 |
| 核心職責 | 定時觸發、發 MQ、不存業務資料 |

> [!IMPORTANT]
> **loancron 只做觸發，不存業務資料**
> - ✅ 定時發 MQ 給其他模組
> - ❌ 不存用戶申請資料
> - ❌ 不直接處理業務邏輯

---

# 2. 核心概念

```
loancron 的職責：
    「什麼時候」做事 → ✅
    「做什麼事」    → ❌ (交給其他模組)

例如：
    loancron: 「現在 00:00，該計息了！」 → 發 MQ
    ledger:   「收到！我來算利息。」    → 實際計息
```

---

# 3. 排程任務清單

### Phase 1

| 任務 | 執行時間 | 目標模組 | 通訊方式 |
|------|---------|---------|---------|
| 處理待審批訂單 (PENDING) | 每 5 分鐘 | loancore | MQ |
| 手動觸發測試 | 按需 | - | HTTP |

### Phase 2

| 任務 | 執行時間 | 目標模組 | 通訊方式 |
|------|---------|---------|---------|
| 日切計息 | 每日 00:00 | ledger | MQ |
| 逾期檢測 | 每日 00:30 | loancore | MQ |
| 狀態遷移 | 每日 01:00 | loancore | MQ |
| 自動扣款 | 每日 09:00 | pay | HTTP |
| 簽約超時 | 每日 02:00 | loancore | MQ |
| 冷靜期結束 | 每日 03:00 | loancore | MQ |

---

# 4. API 清單

| API | 用途 | 呼叫者 | 備註 |
|-----|------|-------|------|
| `POST /loancron/job/{jobName}/trigger` | 手動觸發任務 | 後台 | 測試用 |
| `POST /loancron/job/status` | 查詢任務狀態 | 後台 | |

---

# 5. 程式結構

```
loancron/
├── scheduler/
│   ├── PendingOrderProcessScheduler.java  ← @Scheduled 每 5 分鐘 (處理 PENDING 訂單)
│   ├── EodScheduler.java                  ← @Scheduled 每日 00:00
│   └── AutoDebitScheduler.java            ← @Scheduled 每日 09:00
│
├── producer/
│   ├── LoancoreMqProducer.java            ← 發送「處理 PENDING 訂單」訊息
│   ├── LedgerMqProducer.java              ← 發送「計息」訊息
│   └── CallectMqProducer.java             ← 發送「逾期通知」訊息
│
└── controller/
    └── CronJobController.java             ← 手動觸發 API (測試用)
```

---

# 6. 任務觸發範例

### 處理待審批訂單

```java
@Scheduled(fixedRate = 300000)  // 每 5 分鐘
public void processPendingOrders() {
    // 發送 MQ，讓 loancore 處理 PENDING 狀態的訂單
    loancoreMqProducer.sendProcessPendingOrdersCommand();
}
```

### 日切計息

```java
@Scheduled(cron = "0 0 0 * * *")  // 每日 00:00
public void eodAccrual() {
    // 發送 MQ，讓 ledger 執行計息
    ledgerMqProducer.sendAccrualRunCommand();
}
```

---

# 7. 和其他模組的關係

```
               ┌─────────────┐
               │  loancron   │ ← 只負責觸發
               └──────┬──────┘
                      │ MQ
     ┌────────────────┼────────────────┐
     ▼                ▼                ▼
┌─────────┐     ┌─────────┐     ┌─────────┐
│ origin  │     │ ledger  │     │loancore │
│(處理申請)│     │ (計息)  │     │(狀態遷移)│
└─────────┘     └─────────┘     └─────────┘
                                     │
                                     ▼
                              ┌─────────┐
                              │ callect │
                              │ (催收)  │
                              └─────────┘
```

---

# 8. 設計考量

| 項目 | 建議 |
|-----|------|
| **冪等性** | 任務重複執行不會造成錯誤 |
| **分散式鎖** | 防止多實例重複執行 |
| **不存業務資料** | 只觸發，不儲存 |
| **失敗告警** | 任務失敗要通知 |

---

# 9. 關於 CronJob 表

> [!NOTE]
> **Phase 1 不需要 CronJob 表**
> 
> loancron 只發 MQ，不追蹤執行結果。
> 執行結果由目標模組自己記錄。
> 
> 如果 Phase 2 需要追蹤任務執行歷史，
> 再考慮加入 CronJobHistory 表。
# Loancore Module

這是貸款系統的**總指揮中心**，負責狀態機管理和跨模組協調。

> [!IMPORTANT]
> **loancore 是統一資料來源**
> - origin 收到申請後**立刻**在 loancore 建單 (status=PENDING)
> - 所有貸款流程 (申請/審核/放款/還款/結案) 都在 `LoanOrder` 追蹤
> - 無論最後是自己做、拒絕、還是導流，都在同一張表

---

# 1. 模組定義

| 項目 | 說明 |
|------|------|
| 角色 | 總指揮中心 / Saga 協調器 / **統一資料來源** |
| 對外 API | ⚠️ 僅供內部模組調用 |
| 核心職責 | **統一訂單存儲**、訂單狀態機、跨模組協調、事件發布 |

> [!NOTE]
> **loancore 不直接接收前端請求**，前端入口是 origin。
> origin 收到申請後立刻呼叫 loancore 建單。

---

# 2. 核心流程 (Saga 模式)

#### 階段一：用戶申請 (同步)

```
前端 App
    │
    │ POST /origin/apply
    ▼
┌──────────┐         ┌──────────────┐
│  origin  │────────▶│   loancore   │ 建單 status=PENDING
└──────────┘         └──────────────┘
```

#### 階段二：排程審核 (異步)

```
┌──────────┐         ┌──────────────┐
│ loancron │────MQ──▶│   loancore   │ 1. 收到觸發訊號
└──────────┘         │              │ 2. claimPendingOrders (FOR UPDATE SKIP LOCKED)
                     │              │ 3. 更新狀態為 BUREAU_CHECK
                     │              │ 4. 寫入 Outbox
                     └──────┬───────┘
                            │
                     ┌──────▼───────┐
                     │ Outbox Worker│ 定時發送 MQ
                     └──────┬───────┘
                            │
                     ┌──────▼───────┐
                     │    bureau    │ 收集資料 (TODO)
                     └──────────────┘
```

---

# 3. 訂單狀態定義

| 狀態 | 中文 | 下一步可能狀態 |
|-----|------|--------------|
| PENDING | 待處理 | BUREAU_CHECK |
| BUREAU_CHECK | 徵信查詢中 | UNDERWRITING, REJECTED |
| UNDERWRITING | 審核中 | OFFER_READY, REJECTED, LSP_ROUTING |
| REJECTED | 已拒絕 | (終態) |
| LSP_ROUTING | 轉導合作商 | (終態) |
| OFFER_READY | Offer 已產生 | SIGN_PENDING |
| SIGN_PENDING | 待簽約 | SIGNED, CANCELLED |
| SIGNED | 已簽約 | DISBURSAL_PENDING |
| DISBURSAL_PENDING | 待放款 | DISBURSED, DISBURSAL_FAILED |
| DISBURSAL_FAILED | 放款失敗 | DISBURSAL_PENDING |
| DISBURSED | 已放款 | ACTIVE |
| ACTIVE | 還款中 | OVERDUE, CLOSED |
| OVERDUE | 逾期 | ACTIVE, NPA, CLOSED |
| NPA | 不良資產 | WRITTEN_OFF, CLOSED |
| CLOSED | 已結清 | (終態) |
| WRITTEN_OFF | 已呆帳核銷 | (終態) |
| CANCELLED | 已取消 | (終態) |

---

# 4. API 清單

### Phase 1 (已實作)

| API | 用途 | 呼叫者 |
|-----|------|-------|
| `POST /loancore/order/init` | 建立訂單 | origin |
| `POST /loancore/order/updateState` | 更新狀態 | 各模組 |
| `POST /loancore/order/findById` | 查詢訂單 | App, 後台 |
| `POST /loancore/order/all` | 查詢用戶所有訂單 | App |

### Phase 2

| API | 用途 | 呼叫者 |
|-----|------|-------|
| `POST /loancore/order/{id}/history` | 查詢狀態歷程 | 後台 |
| `POST /loancore/order/{id}/cancel` | 取消申請 | App |
| `POST /loancore/order/{id}/retry` | 重試失敗步驟 | 後台 |

---

# 5. 資料模型

### LoanOrder (已實作) - **統一資料來源**

核心訂單表，**從申請開始就建單**，存儲所有訂單資訊和當前狀態。

> [!NOTE]
> 查用戶貸款狀態只需查 `LoanOrder`，不需要另外查 origin 的 LoanApplication。

### LoanOrderHistory (已實作)

粗粒度歷史記錄，記錄每次狀態變更。

```sql
LoanOrderHistory (
    loanOrderHistoryId,
    loanOrderId,
    fromStatus,
    toStatus,
    triggeredBy,  -- 哪個模組觸發
    remark,
    createdAt
)
```

---

# 6. 和其他模組的關係

```
                    ┌─────────────────────────┐
                    │       loancore          │
                    │ (統一資料來源 + 狀態機)   │
                    └────┬───────────┬─────────┬────┘
                         │           │         │
    ┌─────────────────┼───────────┼─────────┼───────────────┐
    ▼                   ▼           ▼         ▼               ▼
┌─────────┐       ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐
│ origin  │       │ bureau  │  │   sign  │  │   pay   │  │   lsp   │
│(建單+決策)│       │ (徵信)  │  │ (簽約)  │  │ (放款)  │  │ (導流)  │
└─────────┘       └─────────┘  └─────────┘  └─────────┘  └─────────┘
```

**通訊方式：**
- origin → loancore：**MQ (Outbox)**（可靠建單）
- loancore → bureau：**MQ (Outbox)**（可靠發送）
- loancore → origin/sign/pay：**同步 HTTP**（關鍵路徑）
- sign/pay → loancore：**MQ**（狀態更新）
- loancore → lsp：**MQ**（導流）

---

# 7. 設計考量

| 項目 | 建議 |
|-----|------|
| **狀態轉換鎖** | 同一訂單同時只能有一個狀態轉換 |
| **樂觀鎖** | 使用 @Version 防止併發更新 |
| **冪等性** | 狀態更新要支援重複呼叫 |
| **歷程不可變** | History 只能 INSERT |

---

# 8. Outbox Pattern 實作 (已實作)

## 8.1 設計目標

使用 Outbox Pattern 確保 loancore → bureau 的訊息可靠性：
- ✅ 保證訊息不遺失
- ✅ 保證本地事務和訊息發送的原子性
- ✅ 支援失敗重試
- ✅ 使用 Claim-and-Process 避免多台 Server 重複處理

## 8.2 Claim-and-Process 模式

處理 PENDING 訂單時，使用 `FOR UPDATE SKIP LOCKED` 避免多台 Server 重複處理：

```java
@Query(value = """
    SELECT * FROM loan_order
    WHERE loan_state = 'PENDING'
    ORDER BY created_at ASC
    FOR UPDATE SKIP LOCKED
    LIMIT :limit
    """, nativeQuery = true)
List<LoanOrder> claimPendingOrders(@Param("limit") int limit);
```

## 8.3 完整流程

```
@Transactional
1. claimPendingOrders(100)       ← FOR UPDATE SKIP LOCKED 認領
2. updateState(BUREAU_CHECK)     ← 更新狀態（同一個 Transaction）
3. outboxStoreService.saveAll()  ← 批次寫入 Outbox
4. commit                        ← 釋放鎖，狀態已不是 PENDING

Outbox Worker (每 5 秒)
1. claimMessages()               ← 認領 Outbox 訊息
2. rabbitTemplate.send()         ← 發送到 MQ
3. update status = SENT          ← 標記已發送
```

## 8.4 實作檔案

| 檔案 | 用途 |
|-----|------|
| `model/LoancoreOutbox.java` | Outbox Entity |
| `repository/LoancoreOutboxRepository.java` | Outbox Repository |
| `dao/LoancoreOutboxDao.java` | DAO 層 |
| `service/store/LoancoreOutboxStoreService.java` | 寫入 Outbox |
| `service/schedule/LoancoreOutboxScheduleService.java` | 排程發送 |
| `event/LoancoreEvent.java` | 事件發送入口 |

---

# 9. 微服務切分

**不建議獨立拆分**，原因：
1. 核心協調器，和所有模組有關聯
2. 狀態一致性要求高
3. 建議保持在主服務內
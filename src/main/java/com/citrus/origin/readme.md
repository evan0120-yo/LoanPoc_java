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

---

# 9. Outbox Pattern 實作

## 9.1 設計目標

使用 Outbox Pattern 確保 origin → loancore 的訊息可靠性：
- ✅ 保證訊息不遺失（即使 RabbitMQ 暫時不可用）
- ✅ 保證本地事務和訊息發送的原子性
- ✅ 支援失敗重試
- ✅ 提供審計軌跡

## 9.2 需要實作的組件

### 1. OriginOutbox (model)

繼承 `common.OutboxMessage` 抽象基類。

```java
@Entity
@Table(name = "origin_outbox")
@NoArgsConstructor // JPA Entity 必須有無參構造函數
public class OriginOutbox extends OutboxMessage {
    // 繼承所有 protected 欄位
}
```

### 2. OriginOutboxRepository (repository)

繼承 `common.OutboxRepository` 通用介面，自動擁有 claim-and-process 方法。

```java
public interface OriginOutboxRepository extends OutboxRepository<OriginOutbox> {
    // 自動繼承 claimMessages(), findByClaimedByAndStatus() 等方法
}
```

### 3. OriginOutboxDao (dao)

```java
// origin/dao/OriginOutboxDao.java
@Component
@RequiredArgsConstructor
public class OriginOutboxDao {
    
    private final OriginOutboxRepository repository;
    
    public OriginOutbox save(OriginOutbox outbox) {
        outbox.setOutboxId(Generators.defaultTimeBasedGenerator().generate().toString());
        outbox.setCreatedAt(Instant.now());
        return repository.save(outbox);
    }
    
    public OriginOutbox findById(String id) {
        return repository.findById(id).orElse(null);
    }
}
```

### 4. OriginOutboxStoreService (service/store)

提供業務層寫入 Outbox 的功能。

```java
// origin/service/store/OriginOutboxStoreService.java
@Service
@RequiredArgsConstructor
public class OriginOutboxStoreService {
    
    private final OriginOutboxDao outboxDao;
    
    public OriginOutbox save(String aggregateType, String aggregateId, 
                             String eventType, String payload) {  // 直接傳 String
        OriginOutbox outbox = new OriginOutbox();
        outbox.setAggregateType(aggregateType);
        outbox.setAggregateId(aggregateId);
        outbox.setEventType(eventType);
        outbox.setTargetExchange(RabbitMQEnum.ORDER_CREATED.getExchangeName());
        outbox.setTargetRoutingKey(RabbitMQEnum.ORDER_CREATED.getRoutingKey());
        outbox.setPayload(payload);  // 不需要 ObjectMapper
        outbox.setStatus(OutboxStatusEnum.PENDING);
        outbox.setRetryCount(0);
        return outboxDao.save(outbox);
    }
}
```

### 5. OriginOutboxScheduleService (service/schedule)

繼承 `common.OutboxScheduleService` 抽象類，採用 Claim-and-Process 模式。

```java
// origin/service/schedule/OriginOutboxScheduleService.java
@Service
@RequiredArgsConstructor
public class OriginOutboxScheduleService extends OutboxScheduleService<OriginOutbox> {
    
    private final OriginOutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    
    @Value("${outbox.processor-id:origin-${random.uuid}}")
    private String processorId;
    
    @Override
    protected OutboxRepository<OriginOutbox> getOutboxRepository() {
        return outboxRepository;
    }
    
    @Override
    protected String getProcessorId() {
        return processorId;
    }
    
    @Override
    protected void sendMessage(OriginOutbox message) throws Exception {
        // 使用訊息中的路由設定發送到 MQ
        rabbitTemplate.convertAndSend(
            message.getTargetExchange(),
            message.getTargetRoutingKey(),
            message.getPayload()
        );
    }
}
```

### 6. 修改 OriginStoreUsecase

在 `loanApply()` 中寫入 outbox。

```java
// origin/usecase/store/OriginStoreUsecase.java
@Service
@RequiredArgsConstructor
public class OriginStoreUsecase {
    
    private final BlacklistQueryService blacklistQueryService;
    private final OriginOutboxStoreService outboxStoreService;
    private final ObjectMapper objectMapper;
    
    @Transactional
    public void loanApply(LoanApplyReq req) {
        // 1. 檢查黑名單
        List<Blacklist> blacklistList = blacklistQueryService.findUserInExist(req.getUserId());
        if (!blacklistList.isEmpty()) {
            throw new RuntimeException("User is in blacklist");
        }
        
        // 2. 寫入 outbox (與業務邏輯在同一個事務)
        try {
            outboxStoreService.save(
                "LOAN_ORDER",
                UUID.randomUUID().toString(),
                "ORDER_CREATED",
                objectMapper.writeValueAsString(req)
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }
        
        // 3. commit 事務 → 返回成功
    }
}
```

## 9.3 資料表結構

```sql
CREATE TABLE origin_outbox (
    outbox_id VARCHAR(64) PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    target_exchange VARCHAR(100),          -- 目標 MQ Exchange (可選)
    target_routing_key VARCHAR(100),       -- 目標 Routing Key (可選)
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,           -- PENDING / PROCESSING / SENT / FAILED
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    sent_at TIMESTAMP,
    claimed_by VARCHAR(100),               -- 認領的 Server ID
    claimed_at TIMESTAMP,                  -- 認領時間
    error_message VARCHAR(500),
    INDEX idx_status (status, created_at),
    INDEX idx_claimed (claimed_by, status)
);
```

## 9.4 完整流程 (Claim-and-Process)

```
POST /origin/apply
  ├─ 1. checkBlacklist()
  ├─ 2. outboxStoreService.save()  ← 寫入 origin_outbox (status=PENDING)
  └─ 3. commit 事務 → 返回成功

OriginOutboxScheduleService (每 5 秒掃描)
  ├─ Step 1: claimMessages() → UPDATE 認領訊息 (claimed_by=processorId)
  ├─ Step 2: findByClaimedByAndStatus() → 查詢已認領的訊息
  ├─ Step 3: rabbitTemplate.send() → RabbitMQ
  ├─ 成功 → status=SENT
  └─ 失敗 → retryCount++ → 超過 3 次 → status=FAILED
```

## 9.5 監控建議

```sql
-- 待發送訊息數量（應接近 0）
SELECT COUNT(*) FROM origin_outbox WHERE status = 'PENDING';

-- 失敗訊息數量（需警報）
SELECT COUNT(*) FROM origin_outbox WHERE status = 'FAILED';

-- 最舊的待發送訊息（超過 1 分鐘需警報）
SELECT MIN(created_at) FROM origin_outbox WHERE status = 'PENDING';
```


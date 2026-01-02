# Common Module

這是**所有模組的共用模組**，提供可復用的基礎設施組件和通用工具。

> [!IMPORTANT]
> **Common Module 的職責**
> - 提供所有模組共用的基礎設施抽象（Outbox Pattern、分散式鎖等）
> - 定義通用的 Enum、Exception、工具類
> - 避免各模組重複實作相同邏輯
> - 展示「可復用組件」的設計模式

---

# 1. 模組定義

| 項目 | 說明 |
|------|------|
| 角色 | 基礎設施提供者 |
| 對外 API | ❌ 無對外 API |
| 核心職責 | 提供可復用的基礎組件 |
| 資料存儲 | ❌ 不直接存儲資料（但提供 Outbox 抽象） |

---

# 2. 提供的組件

## 2.1 Outbox Pattern（事件發送可靠性保證）

### 設計目標

解決分散式系統中的訊息可靠性問題：
- ✅ 保證訊息不遺失（即使 MQ 暫時不可用）
- ✅ 保證本地事務和訊息發送的原子性
- ✅ 支援失敗重試
- ✅ 提供審計軌跡

### 架構設計（符合三層+1層架構）

```
         ┌─────────────────────────────────────────┐
         │          Usecase 層                       │
         │  (OriginStoreUsecase)                     │
         │  1. 執行業務邏輯                             │
         │  2. 寫入 Outbox (同一個事務)                 │
         └─────────────────┬───────────────────────┘
                           │
         ┌─────────────────▼───────────────────────┐
         │          Service 層                       │
         │  store/XxxOutboxStoreService  (寫入)       │
         │  query/XxxOutboxScheduleService (排程發送) │
         └─────────────────┬───────────────────────┘
                           │
         ┌─────────────────▼───────────────────────┐
         │          Dao 層                           │
         │  (XxxOutboxDao)                           │
         └─────────────────┬───────────────────────┘
                           │
         ┌─────────────────▼───────────────────────┐
         │          Repository 層                    │
         │  (XxxOutboxRepository)                    │
         └─────────────────┬───────────────────────┘
                           │ @Scheduled 排程發送
         ┌─────────────────▼───────────────────────┐
         │          Message Queue (RabbitMQ)         │
         └─────────────────────────────────────────┘
```

### 使用場景

| 場景 | 發送方 | 接收方 | 說明 |
|-----|-------|--------|------|
| **訂單創建** | origin | loancore | 申請時創建訂單 |
| **狀態更新** | loancore | origin | 通知決策結果 |
| **簽約通知** | loancore | sign | 觸發簽約流程 |
| **放款通知** | loancore | pay | 觸發放款流程 |
| **記帳通知** | pay | ledger | 觸發記帳 |

### Common 提供的抽象

#### 2.1.1 OutboxMessage（抽象基類）

各模組繼承此基類創建自己的 Outbox 表。

```java
// common/model/OutboxMessage.java
@MappedSuperclass
public abstract class OutboxMessage {
    @Id
    protected String outboxId;
    protected String aggregateType;    // 聚合根類型 (LOAN_ORDER, PAYMENT...)
    protected String aggregateId;      // 聚合根 ID (orderId, paymentId...)
    protected String eventType;        // 事件類型 (ORDER_CREATED, ORDER_UPDATED...)
    protected String targetExchange;   // 目標 MQ Exchange (可選)
    protected String targetRoutingKey; // 目標 Routing Key (可選)
    protected String payload;          // JSON 格式的事件資料
    
    @Enumerated(EnumType.STRING)
    protected OutboxStatusEnum status; // PENDING / PROCESSING / SENT / FAILED
    
    protected Integer retryCount;      // 重試次數
    protected Instant createdAt;       // 創建時間
    protected Instant sentAt;          // 發送時間
    protected String claimedBy;        // 認領的 Server ID
    protected Instant claimedAt;       // 認領時間
    protected String errorMessage;     // 錯誤訊息（失敗時）
}
```

#### 2.1.2 OutboxStatusEnum（枚舉）

```java
// common/enums/OutboxStatusEnum.java
public enum OutboxStatusEnum {
    PENDING,    // 待發送
    PROCESSING, // 發送中（鎖定狀態，避免重複處理）
    SENT,       // 已發送
    FAILED      // 失敗（超過重試次數）
}
```

#### 2.1.3 OutboxService（服務介面）

提供業務邏輯層寫入 Outbox 的方法。

```java
// common/service/OutboxService.java
public interface OutboxService<T extends OutboxMessage> {
    // 儲存 Outbox 訊息（與業務邏輯在同一個事務中）
    T save(String aggregateType, String aggregateId, String eventType, Object payload);
}
```

> **注意**：Worker 直接透過 Repository 處理狀態更新，此介面僅提供業務層寫入功能。

#### 2.1.4 OutboxRepository（通用 Repository）

提供 Claim-and-Process 模式的核心方法。

```java
// common/repository/OutboxRepository.java
@NoRepositoryBean
public interface OutboxRepository<T extends OutboxMessage> extends JpaRepository<T, String> {
    
    // 認領待發送訊息（原子操作）
    @Modifying
    @Query("UPDATE ... SET status='PROCESSING', claimedBy=:processorId ...")
    int claimMessages(@Param("processorId") String processorId);
    
    // 查詢已認領的訊息
    List<T> findByClaimedByAndStatus(String claimedBy, OutboxStatusEnum status);
    
    // 清除超時未處理的認領（Server 崩潰後恢復）
    @Modifying
    int releaseTimedOutClaims(@Param("minutes") int minutes);
}
```

#### 2.1.5 OutboxScheduleService（排程發送服務基類）

採用 Claim-and-Process 模式，各模組繼承此類放在 `service/schedule/` 路徑。

```java
// common/service/OutboxScheduleService.java
public abstract class OutboxScheduleService<T extends OutboxMessage> {
    
    protected abstract OutboxRepository<T> getOutboxRepository();
    protected abstract String getProcessorId();  // Server 唯一 ID
    protected abstract void sendMessage(T message) throws Exception;
    protected int getMaxRetryCount() { return 3; }
    
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processMessages() {
        OutboxRepository<T> repo = getOutboxRepository();
        String processorId = getProcessorId();
        
        // Step 1: Claim - 認領訊息（原子操作）
        int claimed = repo.claimMessages(processorId);
        if (claimed == 0) return;
        
        // Step 2: Find - 查詢已認領的訊息
        List<T> messages = repo.findByClaimedByAndStatus(processorId, PROCESSING);
        
        // Step 3: Send and Update
        for (T msg : messages) {
            try {
                sendMessage(msg);
                msg.setStatus(SENT);
                msg.setSentAt(Instant.now());
            } catch (Exception e) {
                msg.setRetryCount(msg.getRetryCount() + 1);
                if (msg.getRetryCount() >= getMaxRetryCount()) {
                    msg.setStatus(FAILED);
                } else {
                    msg.setStatus(PENDING);  // 放回重試
                    msg.setClaimedBy(null);
                }
            }
            repo.save(msg);
        }
    }
    
    @Scheduled(fixedDelay = 60000)  // 每分鐘清理超時認領
    @Transactional
    public void releaseTimedOutClaims() {
        getOutboxRepository().releaseTimedOutClaims(5);  // 5 分鐘超時
    }
}
```

---

## 2.2 各模組如何使用（符合三層+1層架構）

### 步驟 1：創建 Model

```java
// origin/model/OriginOutbox.java
@Entity
@Table(name = "origin_outbox")
@NoArgsConstructor
public class OriginOutbox extends OutboxMessage {
    // 繼承 OutboxMessage，可加入模組特有欄位
}
```

### 步驟 2：創建 Repository

```java
// origin/repository/OriginOutboxRepository.java
public interface OriginOutboxRepository extends OutboxRepository<OriginOutbox> {
    // 自動繼承 claimMessages(), findByClaimedByAndStatus() 等方法
}
```

### 步驟 3：創建 Dao

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

### 步驟 4：創建 Service (store)

```java
// origin/service/store/OriginOutboxStoreService.java
@Service
@RequiredArgsConstructor
public class OriginOutboxStoreService {
    
    private final OriginOutboxDao outboxDao;
    private final ObjectMapper objectMapper;
    
    @SneakyThrows
    public OriginOutbox save(String aggregateType, String aggregateId, 
                             String eventType, Object payload) {
        OriginOutbox outbox = new OriginOutbox();
        outbox.setAggregateType(aggregateType);
        outbox.setAggregateId(aggregateId);
        outbox.setEventType(eventType);
        outbox.setPayload(objectMapper.writeValueAsString(payload));
        outbox.setStatus(OutboxStatusEnum.PENDING);
        outbox.setRetryCount(0);
        return outboxDao.save(outbox);
    }
}
```

### 步驟 5：創建 Service (schedule)

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
        rabbitTemplate.convertAndSend(
            message.getTargetExchange(),
            message.getTargetRoutingKey(),
            message.getPayload()
        );
    }
}
```

### 步驟 6：在 Usecase 中使用

```java
// origin/usecase/store/OriginStoreUsecase.java
@Service
@RequiredArgsConstructor
public class OriginStoreUsecase {
    
    private final BlacklistQueryService blacklistQueryService;
    private final OriginOutboxStoreService outboxStoreService;
    
    @Transactional
    public void loanApply(LoanApplyReq req) {
        // 1. 檢查黑名單
        List<Blacklist> blacklistList = blacklistQueryService.findUserInExist(req.getUserId());
        if (!blacklistList.isEmpty()) {
            throw new RuntimeException("User is in blacklist");
        }
        
        // 2. 寫入 Outbox（與業務邏輯在同一個事務）
        outboxStoreService.save(
            "LOAN_ORDER",                    // aggregateType
            UUID.randomUUID().toString(),    // aggregateId
            "ORDER_CREATED",                 // eventType
            req                              // payload
        );
        
        // 3. commit 事務 → 返回成功
    }
}
```

---

## 2.3 Outbox 表結構

各模組需要創建自己的 `outbox_message` 表：

```sql
-- origin 模組
CREATE TABLE origin_outbox_message (
    outbox_id VARCHAR(64) PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    retry_count INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    sent_at TIMESTAMP,
    error_message VARCHAR(500),
    INDEX idx_status (status, created_at)
);

-- loancore 模組
CREATE TABLE loancore_outbox_message (
    -- 相同結構
);

-- ... 其他模組
```

---

## 2.4 設計原則

### 為什麼各模組有自己的 Outbox 表？

1. **符合微服務原則**：各模組資料庫獨立
2. **避免跨庫事務**：Outbox 和業務資料在同一個 DB
3. **獨立部署**：各模組可以獨立重啟 worker

### 為什麼提供抽象而不是直接實作？

1. **展示架構設計能力**：給乙方看如何抽象通用邏輯
2. **靈活性**：各模組可以根據需求自訂（MQ、序列化方式）
3. **符合三層架構**：Usecase → Service → Dao → Repository

---

## 2.5 未來擴展

### 可能新增的組件

| 組件 | 用途 | 優先級 |
|-----|------|-------|
| **DistributedLock** | Redis 分散式鎖抽象 | P1 |
| **IdGenerator** | 分散式 ID 生成器 | P1 |
| **EventBus** | 模組內事件總線 | P2 |
| **RateLimiter** | 限流器抽象 | P2 |

---

## 2.6 目錄結構

```
common/
├─ enums/
│  └─ OutboxStatusEnum.java
├─ model/
│  └─ OutboxMessage.java (抽象基類)
├─ repository/
│  └─ OutboxRepository.java (通用介面)
└─ service/
   └─ OutboxScheduleService.java (排程發送基類)

# 各模組實作範例 (origin)
origin/
├─ model/
│  └─ OriginOutbox.java
├─ repository/
│  └─ OriginOutboxRepository.java
├─ dao/
│  └─ OriginOutboxDao.java
└─ service/
   ├─ store/
   │  └─ OriginOutboxStoreService.java
   └─ schedule/
      └─ OriginOutboxScheduleService.java
```

---

# 3. 設計考量

## 3.1 為什麼需要 Outbox Pattern？

### 問題：本地事務 + 訊息發送的一致性

```
❌ 錯誤做法：
@Transactional
public void processOrder(Order order) {
    orderDao.save(order);            // 1. 儲存訂單
    rabbitTemplate.send(orderEvent); // 2. 發送 MQ
    // commit
}

問題：
- MQ 發送失敗 → 訂單已存，但訊息沒發出去
- MQ 發送成功，commit 失敗 → 訊息發了，但訂單沒存成功
```

```
✅ Outbox 做法：
@Transactional
public void processOrder(Order order) {
    orderDao.save(order);            // 1. 儲存訂單
    outboxDao.save(orderEvent);      // 2. 儲存 Outbox (同一個事務)
    // commit
}

// 異步 Worker
@Scheduled
public void sendMessages() {
    List<OutboxMessage> messages = outboxDao.findPending();
    for (message : messages) {
        rabbitTemplate.send(message); // 3. 發送 MQ
        outboxDao.markAsSent(message);
    }
}

優點：
- 訂單和 Outbox 在同一個事務 → 原子性保證
- 即使 MQ 暫時掛掉，訊息也不會遺失
- Worker 會持續重試，直到發送成功
```

## 3.2 重試策略

- **最大重試次數**：3 次（可配置）
- **重試間隔**：5 秒（通過 worker 掃描週期控制）
- **失敗處理**：超過重試次數後標記為 FAILED，需人工介入

## 3.3 冪等性考量

**發送端（Outbox Worker）**：可能重複發送相同訊息

**接收端（MQ Consumer）**：必須實作冪等性
- 使用 `aggregateId` + `eventType` 作為唯一鍵
- 消費前檢查是否已處理過

---

# 4. 監控建議

## 4.1 Outbox 表監控

```sql
-- 待發送訊息數量（應接近 0）
SELECT COUNT(*) FROM origin_outbox_message WHERE status = 'PENDING';

-- 失敗訊息數量（需警報）
SELECT COUNT(*) FROM origin_outbox_message WHERE status = 'FAILED';

-- 最舊的待發送訊息（超過 1 分鐘需警報）
SELECT MIN(created_at) FROM origin_outbox_message WHERE status = 'PENDING';
```

## 4.2 建議指標

- **Pending 訊息數量** > 100 → 警報（可能 MQ 掛了）
- **Failed 訊息數量** > 0 → 警報（需人工處理）
- **最舊待發送訊息** > 1 分鐘 → 警報（worker 可能掛了）

---

# 5. 參考資料

- [Transactional Outbox Pattern](https://microservices.io/patterns/data/transactional-outbox.html)
- [Saga Pattern](https://microservices.io/patterns/data/saga.html)

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

### 架構設計

```
         ┌─────────────────────────────────────────┐
         │         Business Module                 │
         │  (origin, loancore, pay, sign...)       │
         └─────────────────┬───────────────────────┘
                           │
                           │ 1. 執行業務邏輯
                           │ 2. 寫入 Outbox (同一個事務)
                           │
         ┌─────────────────▼───────────────────────┐
         │          OutboxMessage Table            │
         │  (各模組有自己的 outbox_message 表)     │
         └─────────────────┬───────────────────────┘
                           │
                           │ 3. OutboxWorker 定時掃描
                           │
         ┌─────────────────▼───────────────────────┐
         │            OutboxWorker                 │
         │   (繼承 OutboxWorkerBase 抽象類)        │
         └─────────────────┬───────────────────────┘
                           │
                           │ 4. 發送到 MQ
                           │
         ┌─────────────────▼───────────────────────┐
         │          Message Queue (RabbitMQ)       │
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
    protected String aggregateType;   // 聚合根類型 (LOAN_ORDER, PAYMENT...)
    protected String aggregateId;     // 聚合根 ID (orderId, paymentId...)
    protected String eventType;       // 事件類型 (ORDER_CREATED, ORDER_UPDATED...)
    protected String payload;         // JSON 格式的事件資料
    
    @Enumerated(EnumType.STRING)
    protected OutboxStatus status;    // PENDING / SENT / FAILED
    
    protected Integer retryCount;     // 重試次數
    protected Instant createdAt;      // 創建時間
    protected Instant sentAt;         // 發送時間
    protected String errorMessage;    // 錯誤訊息（失敗時）
}
```

#### 2.1.2 OutboxStatus（枚舉）

```java
// common/enums/OutboxStatus.java
public enum OutboxStatus {
    PENDING,   // 待發送
    SENT,      // 已發送
    FAILED     // 失敗（超過重試次數）
}
```

#### 2.1.3 OutboxService（抽象服務）

提供通用的 Outbox 操作方法。

```java
// common/service/OutboxService.java
public interface OutboxService<T extends OutboxMessage> {
    
    // 儲存 Outbox 訊息
    T save(String aggregateType, String aggregateId, String eventType, Object payload);
    
    // 查詢待發送訊息
    List<T> findPendingMessages(int limit);
    
    // 標記為已發送
    void markAsSent(String outboxId);
    
    // 標記為失敗
    void markAsFailed(String outboxId, String errorMessage);
    
    // 增加重試次數
    void incrementRetryCount(String outboxId);
}
```

#### 2.1.4 OutboxWorkerBase（抽象 Worker）

提供通用的 Outbox Worker 邏輯，各模組繼承並實作發送邏輯。

```java
// common/worker/OutboxWorkerBase.java
public abstract class OutboxWorkerBase<T extends OutboxMessage> {
    
    protected abstract OutboxService<T> getOutboxService();
    protected abstract void sendMessage(T message) throws Exception;
    protected abstract int getMaxRetryCount();
    
    @Scheduled(fixedDelay = 5000) // 每 5 秒掃一次
    public void processMessages() {
        List<T> pendingMessages = getOutboxService().findPendingMessages(100);
        
        for (T message : pendingMessages) {
            try {
                // 發送訊息（由子類實作）
                sendMessage(message);
                
                // 標記為已發送
                getOutboxService().markAsSent(message.getOutboxId());
                
            } catch (Exception e) {
                // 增加重試次數
                getOutboxService().incrementRetryCount(message.getOutboxId());
                
                // 檢查是否超過最大重試次數
                if (message.getRetryCount() >= getMaxRetryCount()) {
                    getOutboxService().markAsFailed(message.getOutboxId(), e.getMessage());
                }
            }
        }
    }
}
```

---

## 2.2 各模組如何使用

### 步驟 1：創建自己的 OutboxMessage 表

```java
// origin/model/OriginOutboxMessage.java
@Entity
@Table(name = "origin_outbox_message")
public class OriginOutboxMessage extends OutboxMessage {
    // 繼承 OutboxMessage，可加入模組特定欄位
}
```

### 步驟 2：實作 OutboxService

```java
// origin/service/OriginOutboxService.java
@Service
public class OriginOutboxService implements OutboxService<OriginOutboxMessage> {
    
    @Autowired
    private OriginOutboxRepository repository;
    
    @Override
    public OriginOutboxMessage save(String aggregateType, String aggregateId, 
                                     String eventType, Object payload) {
        OriginOutboxMessage message = new OriginOutboxMessage();
        message.setOutboxId(UUID.randomUUID().toString());
        message.setAggregateType(aggregateType);
        message.setAggregateId(aggregateId);
        message.setEventType(eventType);
        message.setPayload(objectMapper.writeValueAsString(payload));
        message.setStatus(OutboxStatus.PENDING);
        message.setRetryCount(0);
        message.setCreatedAt(Instant.now());
        
        return repository.save(message);
    }
    
    // ... 其他方法實作
}
```

### 步驟 3：實作 OutboxWorker

```java
// origin/worker/OriginOutboxWorker.java
@Component
public class OriginOutboxWorker extends OutboxWorkerBase<OriginOutboxMessage> {
    
    @Autowired
    private OriginOutboxService outboxService;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Override
    protected OutboxService<OriginOutboxMessage> getOutboxService() {
        return outboxService;
    }
    
    @Override
    protected void sendMessage(OriginOutboxMessage message) throws Exception {
        // 根據 eventType 發送到不同的 exchange/routing key
        rabbitTemplate.convertAndSend(
            "loan.order.exchange",
            message.getEventType(),
            message.getPayload()
        );
    }
    
    @Override
    protected int getMaxRetryCount() {
        return 3; // 最多重試 3 次
    }
}
```

### 步驟 4：在業務邏輯中使用

```java
// origin/usecase/LoanApplyUsecase.java
@Transactional
public String applyLoan(LoanApplyReq req) {
    // 1. 檢查黑名單
    checkBlacklist(req);
    
    // 2. 生成 orderId
    String orderId = UUID.randomUUID().toString();
    
    // 3. 寫入 Outbox（與業務邏輯在同一個事務）
    outboxService.save(
        "LOAN_ORDER",           // aggregateType
        orderId,                // aggregateId
        "ORDER_CREATED",        // eventType
        req                     // payload
    );
    
    // 4. commit 事務後，返回 orderId
    return orderId;
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
3. **最佳實踐**：避免「上帝 Service」，保持單一職責

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
│  └─ OutboxStatus.java
├─ model/
│  └─ OutboxMessage.java (抽象基類)
├─ service/
│  └─ OutboxService.java (介面)
└─ worker/
   └─ OutboxWorkerBase.java (抽象類)
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

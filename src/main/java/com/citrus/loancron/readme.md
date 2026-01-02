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

# 2. Phase 1：處理待審批訂單

## 2.1 流程圖

```
loancron (每 5 分鐘排程)
    │
    ├─ 1. 查詢 loancore 中 status=PENDING 的訂單
    │
    ├─ 2. 對每筆訂單發送 MQ (REVIEW_ORDER)
    │
    └─ 3. MQ → loancore
              │
              ├─ 呼叫 bureau (徵信)
              ├─ 呼叫 origin (決策引擎)
              └─ 更新訂單狀態
```

## 2.2 需要的組件

### 目錄結構

```
loancron/
├── scheduler/
│   └── PendingOrderScheduler.java      ← @Scheduled 每 5 分鐘
├── service/
│   ├── query/
│   │   └── LoancronQueryService.java   ← 查詢 PENDING 訂單
│   └── store/
│       └── LoancronOutboxStoreService.java  ← 寫入 Outbox
├── dao/
│   └── LoancronOutboxDao.java
├── repository/
│   └── LoancronOutboxRepository.java
├── model/
│   └── LoancronOutbox.java
└── event/
    └── LoancronEvent.java              ← 發送 REVIEW_ORDER 事件
```

---

# 3. 實作細節

## 3.1 新增 RabbitMQ Enum

```java
// share/enums/RabbitMQEnum.java
REVIEW_ORDER(
    "LOAN_ORDER",
    "loan.review.exchange",
    ExchangeTypeEnum.TOPIC,
    List.of(
        QueueConstants.LOANCORE_REVIEW_ORDER
    ),
    "order.review"
),
```

## 3.2 新增 Queue 常量

```java
// share/constants/QueueConstants.java
public static final String LOANCORE_REVIEW_ORDER = "loancore.order.review.queue";
```

## 3.3 ShedLock 分散式鎖（重要！）

> [!CAUTION]
> **多台 Server 同時執行排程會造成重複處理！**
> 
> 如果部署 3 台 Server，每 5 分鐘每台都會觸發排程，
> 同一筆訂單會被發送 3 次 MQ，造成重複審核。
> 
> **必須使用 ShedLock 確保只有一台 Server 執行排程！**

### 3.3.1 什麼是 ShedLock？

ShedLock 是一個輕量級的分散式鎖套件，用 **資料庫** 作為鎖的存儲，確保排程任務在多台 Server 中只執行一次。

```
Server 1: @Scheduled → 取得鎖 → 執行 ✅
Server 2: @Scheduled → 鎖被 Server 1 持有 → 跳過 ❌
Server 3: @Scheduled → 鎖被 Server 1 持有 → 跳過 ❌
```

### 3.3.2 Maven 依賴

```xml
<!-- pom.xml -->
<dependency>
    <groupId>net.javacrumbs.shedlock</groupId>
    <artifactId>shedlock-spring</artifactId>
    <version>5.10.0</version>
</dependency>
<dependency>
    <groupId>net.javacrumbs.shedlock</groupId>
    <artifactId>shedlock-provider-jdbc-template</artifactId>
    <version>5.10.0</version>
</dependency>
```

### 3.3.3 建立資料表

```sql
-- ShedLock 需要的資料表（與業務資料同一個 DB）
CREATE TABLE shedlock (
    name VARCHAR(64) NOT NULL PRIMARY KEY,
    lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by VARCHAR(255) NOT NULL
);
```

### 3.3.4 啟用 ShedLock

```java
// common/config/ShedLockConfig.java
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class ShedLockConfig {

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
            JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(new JdbcTemplate(dataSource))
                .usingDbTime()  // 使用 DB 時間，避免各 Server 時間不同步
                .build()
        );
    }
}
```

### 3.3.5 排程器使用 @SchedulerLock

```java
// loancron/scheduler/PendingOrderScheduler.java
@Component
@RequiredArgsConstructor
@Slf4j
public class PendingOrderScheduler {

    private final LoancronQueryService queryService;
    private final LoancronEvent loancronEvent;

    /**
     * 每 5 分鐘處理 PENDING 訂單
     * 
     * @SchedulerLock 確保多台 Server 只有一台執行：
     * - name: 鎖的名稱（唯一）
     * - lockAtLeastFor: 最少鎖多久（防止執行太快重複觸發）
     * - lockAtMostFor: 最多鎖多久（防止 Server 崩潰鎖永遠不釋放）
     */
    @Scheduled(fixedDelay = 300000)  // 每 5 分鐘
    @SchedulerLock(name = "processPendingOrders", lockAtLeastFor = "4m", lockAtMostFor = "5m")
    public void processPendingOrders() {
        log.info("=== Start Processing Pending Orders ===");
        
        List<String> pendingOrderIds = queryService.findPendingOrderIds();
        
        if (pendingOrderIds.isEmpty()) {
            log.info("No pending orders found");
            return;
        }
        
        log.info("Found {} pending orders to review", pendingOrderIds.size());
        
        for (String orderId : pendingOrderIds) {
            loancronEvent.reviewOrderEvent(orderId);
        }
        
        log.info("=== End Processing Pending Orders ===");
    }
}
```

### 3.3.6 參數說明

| 參數 | 說明 | 範例 |
|------|------|------|
| `name` | 鎖名稱，必須唯一 | `"processPendingOrders"` |
| `lockAtLeastFor` | 最少鎖定時間，防止快速重複執行 | `"4m"` (4 分鐘) |
| `lockAtMostFor` | 最長鎖定時間，防止 Server 崩潰永遠不解鎖 | `"5m"` (5 分鐘) |

> [!TIP]
> **lockAtLeastFor 應該略小於排程間隔**
> 
> 排程每 5 分鐘執行一次 → `lockAtLeastFor = "4m"`
> 
> 這樣即使任務很快完成，也能確保在下次排程之前不會重複執行。

### 3.3.7 生產環境：使用 Redis（高併發推薦）

> [!IMPORTANT]
> **高併發場景（3000+/s）建議使用 Redis 版本**
> 
> | 環境 | 建議 Provider |
> |------|--------------|
> | POC / 開發 | DB (jdbc-template) |
> | 生產 / 高併發 | Redis |

#### Redis 版 Maven 依賴

```xml
<!-- pom.xml - 生產環境 -->
<dependency>
    <groupId>net.javacrumbs.shedlock</groupId>
    <artifactId>shedlock-spring</artifactId>
    <version>5.10.0</version>
</dependency>
<dependency>
    <groupId>net.javacrumbs.shedlock</groupId>
    <artifactId>shedlock-provider-redis-spring</artifactId>
    <version>5.10.0</version>
</dependency>
```

#### Redis 版 Config

```java
// common/config/ShedLockConfig.java (生產版)
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class ShedLockConfig {

    @Bean
    public LockProvider lockProvider(RedisConnectionFactory connectionFactory) {
        return new RedisLockProvider(connectionFactory);
    }
}
```

#### 比較

| 項目 | DB 版本 | Redis 版本 |
|------|--------|-----------|
| 效能 | 較慢 | 快速 |
| 額外架構 | 無需 | 需要 Redis |
| 鎖精度 | 毫秒級 | 毫秒級 |
| 適合場景 | 排程間隔長 | 高頻排程、高併發 |

> [!NOTE]
> **程式碼不用改！** 
> 
> 從 DB 切換到 Redis 只需：
> 1. 換 Maven 依賴
> 2. 改 `LockProvider` Bean
> 
> `@SchedulerLock` 註解和參數完全相同。

---

## 3.4 查詢服務

```java
// loancron/service/query/LoancronQueryService.java
@Service
@RequiredArgsConstructor
public class LoancronQueryService {

    private final LoanOrderRepository loanOrderRepository;

    public List<String> findPendingOrderIds() {
        // 查詢 loancore 的 LoanOrder 表，狀態為 PENDING
        return loanOrderRepository.findIdsByStatus(LoanStateEnum.PENDING);
    }
}
```

## 3.5 發送事件 (Outbox 模式)

```java
// loancron/event/LoancronEvent.java
@Component
@RequiredArgsConstructor
@Slf4j
public class LoancronEvent {

    private final LoancronOutboxStoreService outboxStoreService;
    private final Gson gson;

    public void reviewOrderEvent(String orderId) {
        ReviewOrderEventDto dto = ReviewOrderEventDto.builder()
            .orderId(orderId)
            .triggerAt(Instant.now())
            .build();
        
        outboxStoreService.save(
            RabbitMQEnum.REVIEW_ORDER.getAggregateType(),
            orderId,
            RabbitMQEnum.REVIEW_ORDER.name(),
            gson.toJson(dto)
        );
        
        log.info("Sent review order event for orderId: {}", orderId);
    }
}
```

## 3.6 Loancore Consumer

```java
// loancore/consumer/ReviewOrderConsumer.java
@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewOrderConsumer {

    private final Gson gson;
    private final ReviewOrderUsecase reviewOrderUsecase;

    @RabbitListener(queues = QueueConstants.LOANCORE_REVIEW_ORDER)
    public void handleReviewOrder(String message) {
        log.info("=== Received Review Order Event ===");
        
        ReviewOrderEventDto dto = gson.fromJson(message, ReviewOrderEventDto.class);
        log.info("orderId: {}", dto.getOrderId());
        
        // 觸發審核流程
        reviewOrderUsecase.review(dto.getOrderId());
        
        log.info("=== End of Review Order Processing ===");
    }
}
```

---

# 4. 完整流程

```
User 打 API
    │
    ▼
origin/loanApply
    │
    ├─ 1. checkBlacklist
    ├─ 2. originEvent.loanApplyEvent()  → origin_outbox (PENDING)
    │
    ▼
OriginOutboxScheduleService (每 5 秒)
    │
    └─ 發送到 RabbitMQ (ORDER_CREATED)
              │
              ▼
loancore/LoanOrderConsumer
    │
    └─ 建立 LoanOrder (status=PENDING)

────────────────────────────────────────

loancron/PendingOrderScheduler (每 5 分鐘)
    │
    ├─ 1. 查詢 PENDING 訂單
    ├─ 2. loancronEvent.reviewOrderEvent() → loancron_outbox (PENDING)
    │
    ▼
LoancronOutboxScheduleService (每 5 秒)
    │
    └─ 發送到 RabbitMQ (REVIEW_ORDER)
              │
              ▼
loancore/ReviewOrderConsumer
    │
    ├─ 1. 呼叫 bureau (徵信查詢)
    ├─ 2. 呼叫 origin (決策引擎)
    └─ 3. 更新訂單狀態 (APPROVED/REJECTED/LSP_ROUTING)
```

---

# 5. Phase 2 任務 (之後實作)

| 任務 | 執行時間 | 目標模組 | 通訊方式 |
|------|---------|---------|---------| 
| 日切計息 | 每日 00:00 | ledger | MQ |
| 逾期檢測 | 每日 00:30 | loancore | MQ |
| 狀態遷移 | 每日 01:00 | loancore | MQ |
| 自動扣款 | 每日 09:00 | pay | HTTP |
| 簽約超時 | 每日 02:00 | loancore | MQ |
| 冷靜期結束 | 每日 03:00 | loancore | MQ |

---

# 6. 資料表結構

```sql
CREATE TABLE loancron_outbox (
    outbox_id VARCHAR(64) PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    target_exchange VARCHAR(100),
    target_routing_key VARCHAR(100),
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    sent_at TIMESTAMP,
    claimed_by VARCHAR(100),
    claimed_at TIMESTAMP,
    error_message VARCHAR(500),
    INDEX idx_status (status, created_at),
    INDEX idx_claimed (claimed_by, status)
);
```

---

# 7. 設計考量

| 項目 | 建議 |
|-----|------|
| **冪等性** | 任務重複執行不會造成錯誤 (loancore 判斷是否已處理) |
| **Outbox Pattern** | 確保 MQ 訊息可靠發送 |
| **不存業務資料** | 只觸發，不儲存 (業務資料在 loancore) |
| **失敗告警** | 任務失敗要通知 |
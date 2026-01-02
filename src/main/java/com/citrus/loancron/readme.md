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
> **loancron 只做觸發，不碰其他模組的資料**
> - ✅ 定時發 MQ 觸發訊號給其他模組
> - ❌ 不查詢 loancore 的資料
> - ❌ 不存用戶申請資料
> - ❌ 不直接處理業務邏輯

---

# 2. Phase 1：觸發待審批訂單審核

## 2.1 核心設計

> [!CAUTION]
> **loancron 只發「開始審核」訊號，不帶任何業務資料！**
> 
> loancore 收到觸發訊號後，自己查詢 PENDING 訂單並處理。
> 這樣才能保持模組解耦，符合微服務原則。

## 2.2 流程圖

```
loancron (每 20 秒 / 生產用 5 分鐘)
    │
    └─ 發送「開始審核」觸發訊號 (不帶 orderId)
              │
              ▼
loancore/ReviewOrderConsumer
    │
    ├─ 自己查詢 PENDING 訂單
    ├─ 呼叫 bureau (徵信)
    ├─ 呼叫 origin (決策引擎)
    └─ 更新訂單狀態
```

## 2.3 目錄結構

```
loancron/
├── config/
│   └── ShedLockConfig.java         ← 分散式鎖配置
├── scheduler/
│   └── PendingOrderScheduler.java  ← @Scheduled 定時觸發
├── event/
│   └── LoancronEvent.java          ← 發送觸發訊號
├── service/
│   └── store/
│       └── LoancronOutboxStoreService.java  ← 寫入 Outbox
├── dao/
│   └── LoancronOutboxDao.java
├── repository/
│   └── LoancronOutboxRepository.java
└── model/
    ├── LoancronOutbox.java
    └── Shedlock.java               ← 讓 Hibernate 自動建表
```

---

# 3. 實作細節

## 3.1 RabbitMQ 設定

```java
// share/enums/RabbitMQEnum.java
REVIEW_ORDER(
    "LOAN_ORDER",
    "loan.review.exchange",
    ExchangeTypeEnum.TOPIC,
    List.of(QueueConstants.LOANCORE_REVIEW_ORDER),
    "order.review"
),

// share/constants/QueueConstants.java
public static final String LOANCORE_REVIEW_ORDER = "loancore.order.review.queue";
```

## 3.2 排程器（只發觸發訊號）

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class PendingOrderScheduler {

    private final LoancronEvent loancronEvent;

    @Scheduled(fixedDelay = 20000) // 每 20 秒（測試用，生產改回 5 分鐘）
    @SchedulerLock(name = "processPendingOrders", lockAtLeastFor = "15s", lockAtMostFor = "20s")
    public void triggerPendingOrderReview() {
        log.info("=== Trigger Pending Order Review ===");
        
        // 只發送觸發訊號，不查詢任何 loancore 資料
        loancronEvent.triggerReviewEvent();
        
        log.info("=== Trigger Sent ===");
    }
}
```

## 3.3 事件發送（不帶業務資料）

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class LoancronEvent {

    private final LoancronOutboxStoreService outboxStoreService;
    private final Gson gson;

    public void triggerReviewEvent() {
        // 只發送觸發時間，不帶業務資料
        TriggerEventPayload payload = new TriggerEventPayload(Instant.now());

        outboxStoreService.save(
                RabbitMQEnum.REVIEW_ORDER.getAggregateType(),
                "TRIGGER",  // aggregateId 用固定值
                RabbitMQEnum.REVIEW_ORDER.name(),
                gson.toJson(payload));
    }

    private record TriggerEventPayload(Instant triggerAt) {}
}
```

## 3.4 Loancore Consumer（自己查詢資料）

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewOrderConsumer {

    private final LoanOrderRepository loanOrderRepository;

    @RabbitListener(queues = QueueConstants.LOANCORE_REVIEW_ORDER)
    public void handleReviewTrigger(String message) {
        log.info("=== Received Review Trigger ===");

        // 自己查詢 PENDING 訂單
        List<LoanOrder> pendingOrders = loanOrderRepository.findByLoanState(LoanStateEnum.PENDING);

        if (pendingOrders.isEmpty()) {
            log.info("No pending orders found");
            return;
        }

        log.info("Found {} pending orders to review", pendingOrders.size());

        for (LoanOrder order : pendingOrders) {
            log.info("Processing order: {}", order.getLoanOrderId());
            // TODO: 觸發審核流程
        }

        log.info("=== End of Review Processing ===");
    }
}
```

---

# 4. ShedLock 分散式鎖

> [!CAUTION]
> **多台 Server 同時執行排程會造成重複觸發！**
> 必須使用 ShedLock 確保只有一台 Server 執行排程。

## 4.1 原理

```
Server 1: @Scheduled → 取得鎖 → 執行 ✅
Server 2: @Scheduled → 鎖被 Server 1 持有 → 跳過 ❌
Server 3: @Scheduled → 鎖被 Server 1 持有 → 跳過 ❌
```

## 4.2 Maven 依賴

```xml
<!-- ShedLock - 分散式排程鎖 -->
<dependency>
    <groupId>net.javacrumbs.shedlock</groupId>
    <artifactId>shedlock-spring</artifactId>
    <version>5.10.0</version>
</dependency>
<!-- ShedLock - DB 版本 (POC/開發用)，生產可換成 shedlock-provider-redis-spring -->
<dependency>
    <groupId>net.javacrumbs.shedlock</groupId>
    <artifactId>shedlock-provider-jdbc-template</artifactId>
    <version>5.10.0</version>
</dependency>
```

## 4.3 Config

```java
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

## 4.4 生產環境用 Redis

| 環境 | 建議 Provider |
|------|--------------|
| POC / 開發 | DB (jdbc-template) |
| 生產 / 高併發 | Redis |

```java
// 生產版 Config
@Bean
public LockProvider lockProvider(RedisConnectionFactory connectionFactory) {
    return new RedisLockProvider(connectionFactory);
}
```

---

# 5. 完整流程

```
User 打 API
    │
    ▼
origin/loanApply
    │
    ├─ checkBlacklist
    └─ originEvent.loanApplyEvent() → origin_outbox
              │
              ▼
OriginOutboxScheduleService (每 5 秒)
    │
    └─ 發送到 RabbitMQ (ORDER_CREATED)
              │
              ▼
loancore/LoanOrderConsumer
    │
    └─ 建立 LoanOrder (loanState=PENDING)

────────────────────────────────────────

loancron/PendingOrderScheduler (每 20 秒)
    │
    └─ triggerReviewEvent() → loancron_outbox (只發觸發訊號)
              │
              ▼
LoancronOutboxScheduleService (每 5 秒)
    │
    └─ 發送到 RabbitMQ (REVIEW_ORDER)
              │
              ▼
loancore/ReviewOrderConsumer
    │
    ├─ 自己查詢 PENDING 訂單
    ├─ 呼叫 bureau (徵信)
    ├─ 呼叫 origin (決策引擎)
    └─ 更新訂單狀態
```

---

# 6. Phase 2 任務（之後實作）

| 任務 | 執行時間 | 目標模組 | 通訊方式 |
|------|---------|---------|---------| 
| 日切計息 | 每日 00:00 | ledger | MQ |
| 逾期檢測 | 每日 00:30 | loancore | MQ |
| 狀態遷移 | 每日 01:00 | loancore | MQ |
| 自動扣款 | 每日 09:00 | pay | HTTP |
| 簽約超時 | 每日 02:00 | loancore | MQ |
| 冷靜期結束 | 每日 03:00 | loancore | MQ |

---

# 7. 設計考量

| 項目 | 建議 |
|-----|------|
| **模組解耦** | loancron 不查詢其他模組資料，只發觸發訊號 |
| **ShedLock** | 確保多台 Server 只有一台執行排程 |
| **Outbox Pattern** | 確保 MQ 訊息可靠發送 |
| **冪等性** | loancore 處理時判斷是否已處理 |
| **不存業務資料** | 業務資料由各模組自己管理 |
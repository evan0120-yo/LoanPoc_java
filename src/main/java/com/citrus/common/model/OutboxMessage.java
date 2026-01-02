package com.citrus.common.model;

import java.time.Instant;

import com.citrus.common.enums.OutboxStatusEnum;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

/**
 * Outbox Pattern 抽象基類
 * 各模組繼承此類創建自己的 outbox_message 表
 * 
 * 使用 @MappedSuperclass：
 * - 子類會繼承所有欄位
 * - 基類本身不會建立表
 * - 符合微服務「各自資料庫」原則
 */
@Data
@MappedSuperclass
public abstract class OutboxMessage {

    @Id
    @Column(name = "outbox_id", length = 64)
    protected String outboxId;

    @Column(name = "aggregate_type", length = 50, nullable = false)
    protected String aggregateType; // 聚合根類型 (LOAN_ORDER, PAYMENT...)

    @Column(name = "aggregate_id", length = 64, nullable = false)
    protected String aggregateId; // 聚合根 ID (orderId, paymentId...)

    @Column(name = "event_type", length = 50, nullable = false)
    protected String eventType; // 事件類型 (ORDER_CREATED, ORDER_UPDATED...)

    @Column(name = "target_exchange", length = 100)
    protected String targetExchange; // 目標 MQ Exchange (可選，讓 Worker 更通用)

    @Column(name = "target_routing_key", length = 100)
    protected String targetRoutingKey; // 目標 Routing Key (可選)

    @Column(name = "payload", columnDefinition = "TEXT", nullable = false)
    protected String payload; // JSON 格式的事件資料

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    protected OutboxStatusEnum status; // PENDING / SENT / FAILED

    @Column(name = "retry_count", nullable = false)
    protected Integer retryCount; // 重試次數

    @Column(name = "created_at", nullable = false)
    protected Instant createdAt; // 創建時間

    @Column(name = "sent_at")
    protected Instant sentAt; // 發送時間

    @Column(name = "claimed_by", length = 100)
    protected String claimedBy; // 認領的 Server ID (e.g., "origin-server-1")

    @Column(name = "claimed_at")
    protected Instant claimedAt; // 認領時間

    @Column(name = "error_message", length = 500)
    protected String errorMessage; // 錯誤訊息（失敗時）
}

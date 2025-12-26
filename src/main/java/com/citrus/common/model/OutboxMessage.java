package com.citrus.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Outbox Pattern 訊息表
 * 用於確保 MQ 與 DB 的最終一致性
 */
@Entity
@Table(name = "outbox_messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxMessage extends BaseModel {

    /**
     * 聚合類型 (e.g., LOAN, PAYMENT)
     */
    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    /**
     * 聚合 ID
     */
    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;

    /**
     * 事件類型 (e.g., LOAN_CREATED, PAYMENT_RECEIVED)
     */
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    /**
     * 事件內容 (JSON)
     */
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    /**
     * 訊息狀態
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private OutboxStatus status = OutboxStatus.PENDING;

    /**
     * 重試次數
     */
    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    /**
     * 下次重試時間
     */
    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    /**
     * 錯誤訊息
     */
    @Column(name = "error_message", length = 500)
    private String errorMessage;

    public enum OutboxStatus {
        PENDING, // 待發送
        SENT, // 已發送
        FAILED // 發送失敗
    }
}

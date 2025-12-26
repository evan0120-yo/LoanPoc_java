package com.citrus.pay.model;

import com.citrus.common.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 收款單 (還款連結)
 */
@Entity
@Table(name = "collection_orders", indexes = {
        @Index(name = "idx_collection_loan", columnList = "loanRecordId"),
        @Index(name = "idx_collection_ref", columnList = "pgRef")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionOrder extends BaseModel {

    @Column(name = "loan_record_id", nullable = false)
    private Long loanRecordId;

    @Column(name = "emi_schedule_id")
    private Long emiScheduleId;

    @Column(name = "expected_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal expectedAmount;

    @Column(name = "actual_amount", precision = 15, scale = 2)
    private BigDecimal actualAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", length = 20)
    private PaymentMode paymentMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CollectionStatus status = CollectionStatus.PENDING;

    @Column(name = "payment_link", length = 500)
    private String paymentLink;

    @Column(name = "upi_intent", length = 500)
    private String upiIntent;

    @Column(name = "pg_ref", length = 100)
    private String pgRef;

    @Column(name = "utr", length = 50)
    private String utr;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    public enum PaymentMode {
        UPI,
        NETBANKING,
        CARD,
        NACH,
        CASH
    }

    public enum CollectionStatus {
        PENDING, // 待收款
        LINK_SENT, // 連結已發送
        PROCESSING, // 處理中
        SUCCESS, // 成功
        FAILED, // 失敗
        EXPIRED, // 已過期
        PARTIAL // 部分還款
    }
}

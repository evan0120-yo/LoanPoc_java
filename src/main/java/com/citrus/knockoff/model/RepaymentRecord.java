package com.citrus.knockoff.model;

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

/**
 * 還款沖銷紀錄
 * 記錄還款如何分配到各個科目
 */
@Entity
@Table(name = "repayment_records", indexes = {
        @Index(name = "idx_repayment_loan", columnList = "loanRecordId"),
        @Index(name = "idx_repayment_collection", columnList = "collectionOrderId")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepaymentRecord extends BaseModel {

    @Column(name = "loan_record_id", nullable = false)
    private Long loanRecordId;

    @Column(name = "collection_order_id")
    private Long collectionOrderId;

    @Column(name = "utr", length = 50)
    private String utr;

    // ===== 收到金額 =====
    @Column(name = "received_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal receivedAmount;

    // ===== 瀑布流分配 =====
    /** GST 稅金 */
    @Column(name = "gst_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal gstAmount = BigDecimal.ZERO;

    /** 滯納金/罰息 */
    @Column(name = "penalty_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal penaltyAmount = BigDecimal.ZERO;

    /** 利息 */
    @Column(name = "interest_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal interestAmount = BigDecimal.ZERO;

    /** 本金 */
    @Column(name = "principal_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal principalAmount = BigDecimal.ZERO;

    /** 溢繳金額 */
    @Column(name = "excess_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal excessAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private RepaymentStatus status = RepaymentStatus.PENDING;

    public enum RepaymentStatus {
        PENDING, // 待處理
        ALLOCATED, // 已分配
        POSTED // 已入帳
    }
}

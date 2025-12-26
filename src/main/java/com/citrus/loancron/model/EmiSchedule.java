package com.citrus.loancron.model;

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
import java.time.LocalDate;

/**
 * EMI 分期計劃表
 */
@Entity
@Table(name = "emi_schedules", indexes = {
        @Index(name = "idx_emi_loan", columnList = "loanRecordId"),
        @Index(name = "idx_emi_due_date", columnList = "dueDate")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmiSchedule extends BaseModel {

    @Column(name = "loan_record_id", nullable = false)
    private Long loanRecordId;

    @Column(name = "installment_no", nullable = false)
    private Integer installmentNo;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    // ===== 應繳金額 =====
    @Column(name = "emi_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal emiAmount;

    @Column(name = "principal_component", nullable = false, precision = 15, scale = 2)
    private BigDecimal principalComponent;

    @Column(name = "interest_component", nullable = false, precision = 15, scale = 2)
    private BigDecimal interestComponent;

    // ===== 實際繳納 =====
    @Column(name = "paid_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    // ===== 滯納金 =====
    @Column(name = "penalty_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal penaltyAmount = BigDecimal.ZERO;

    // ===== 狀態 =====
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EmiStatus status = EmiStatus.UPCOMING;

    @Column(name = "dpd")
    @Builder.Default
    private Integer dpd = 0;

    public enum EmiStatus {
        UPCOMING, // 即將到期
        DUE, // 已到期
        PAID, // 已繳清
        PARTIAL, // 部分繳納
        OVERDUE // 逾期
    }

    /**
     * 計算剩餘應繳
     */
    public BigDecimal getOutstanding() {
        return emiAmount.add(penaltyAmount).subtract(paidAmount);
    }
}

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

/**
 * 放款單
 */
@Entity
@Table(name = "disbursal_orders", indexes = {
        @Index(name = "idx_disbursal_loan", columnList = "loanRecordId"),
        @Index(name = "idx_disbursal_utr", columnList = "utr")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisbursalOrder extends BaseModel {

    @Column(name = "loan_record_id", nullable = false)
    private Long loanRecordId;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "bank_account", nullable = false, length = 20)
    private String bankAccount;

    @Column(name = "ifsc_code", nullable = false, length = 11)
    private String ifscCode;

    @Column(name = "beneficiary_name", length = 100)
    private String beneficiaryName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private DisbursalStatus status = DisbursalStatus.PENDING;

    @Column(name = "utr", length = 50)
    private String utr;

    @Column(name = "pg_ref", length = 100)
    private String pgRef;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    public enum DisbursalStatus {
        PENDING, // 待放款
        PROCESSING, // 處理中
        SUCCESS, // 成功
        FAILED, // 失敗
        REVERSED // 已沖正
    }
}

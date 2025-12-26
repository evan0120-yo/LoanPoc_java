package com.citrus.loancore.model;

import com.citrus.common.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 貸款記錄主表 - 總狀態機
 */
@Entity
@Table(name = "loan_records", indexes = {
        @Index(name = "idx_loan_user_id", columnList = "userId"),
        @Index(name = "idx_loan_mobile", columnList = "mobile"),
        @Index(name = "idx_loan_pan", columnList = "panNumber"),
        @Index(name = "idx_loan_state", columnList = "state")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanRecord extends BaseModel {

    // ========== 使用者資訊 ==========
    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "mobile", nullable = false, length = 15)
    private String mobile;

    @Column(name = "pan_number", nullable = false, length = 10)
    private String panNumber;

    @Column(name = "name", length = 100)
    private String name;

    // ========== 貸款資訊 ==========
    @Column(name = "applied_amount", precision = 15, scale = 2)
    private BigDecimal appliedAmount;

    @Column(name = "approved_amount", precision = 15, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "disbursed_amount", precision = 15, scale = 2)
    private BigDecimal disbursedAmount;

    @Column(name = "roi", precision = 5, scale = 2)
    private BigDecimal roi;

    @Column(name = "tenure")
    private Integer tenure;

    @Column(name = "emi_amount", precision = 15, scale = 2)
    private BigDecimal emiAmount;

    // ========== 日期資訊 ==========
    @Column(name = "application_date")
    private LocalDate applicationDate;

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    @Column(name = "disbursal_date")
    private LocalDate disbursalDate;

    @Column(name = "first_emi_date")
    private LocalDate firstEmiDate;

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Column(name = "closure_date")
    private LocalDate closureDate;

    // ========== 狀態機 ==========
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 30)
    @Builder.Default
    private LoanState state = LoanState.PENDING;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    // ========== 銀行資訊 ==========
    @Column(name = "bank_account", length = 20)
    private String bankAccount;

    @Column(name = "ifsc_code", length = 11)
    private String ifscCode;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    // ========== UTR 追蹤 ==========
    @Column(name = "disbursal_utr", length = 50)
    private String disbursalUtr;

    // ========== 版本控制 (樂觀鎖) ==========
    @Column(name = "version")
    @Builder.Default
    private Long version = 0L;

    /**
     * 貸款狀態機
     */
    public enum LoanState {
        // 申請階段
        PENDING, // 待處理
        BUREAU_CHECK, // 徵信查詢中

        // 審核階段
        UNDERWRITING, // 審核中
        REJECTED, // 已拒絕
        LSP_ROUTING, // 轉導合作商

        // 簽約階段
        OFFER_READY, // Offer 已產生
        SIGN_PENDING, // 待簽約
        SIGNED, // 已簽約

        // 放款階段
        DISBURSAL_PENDING, // 待放款
        DISBURSAL_FAILED, // 放款失敗
        DISBURSED, // 已放款

        // 還款階段
        ACTIVE, // 還款中
        OVERDUE, // 逾期
        NPA, // 不良資產 (90+ DPD)

        // 結案
        CLOSED, // 已結清
        WRITTEN_OFF, // 已呆帳核銷
        CANCELLED // 已取消
    }
}

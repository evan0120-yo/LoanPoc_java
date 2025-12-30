package com.citrus.loancore.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.citrus.loancore.enums.LoanStateEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "loan_order")
public class LoanOrder {

    // 基本資訊
    @Id
    private String loanOrderId;
    private String userId;
    private String mobile;
    private String panNumber;
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
    // ========== 精準時間戳（用 Instant）==========
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Column(name = "state_changed_at")
    private Instant stateChangedAt;
    @Column(name = "disbursed_at")
    private Instant disbursedAt;
    @Column(name = "closed_at")
    private Instant closedAt;
    // ========== 狀態機 ==========
    @Enumerated(EnumType.STRING)
    private LoanStateEnum loanState;
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
    @Version
    private Long version;
}
package com.citrus.lsp.model;

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
 * 導流記錄 (Lead)
 */
@Entity
@Table(name = "lead_records", indexes = {
        @Index(name = "idx_lead_loan", columnList = "loanRecordId"),
        @Index(name = "idx_lead_partner", columnList = "partnerCode")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadRecord extends BaseModel {

    @Column(name = "loan_record_id", nullable = false)
    private Long loanRecordId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "pan_number", nullable = false, length = 10)
    private String panNumber;

    @Column(name = "mobile", nullable = false, length = 15)
    private String mobile;

    // ===== 合作商 =====
    @Column(name = "partner_code", nullable = false, length = 20)
    private String partnerCode;

    @Column(name = "partner_name", length = 100)
    private String partnerName;

    @Column(name = "waterfall_priority")
    private Integer waterfallPriority;

    // ===== 狀態 =====
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private LeadStatus status = LeadStatus.PENDING;

    @Column(name = "partner_ref", length = 100)
    private String partnerRef;

    @Column(name = "reject_reason", length = 200)
    private String rejectReason;

    // ===== 佣金 =====
    @Column(name = "commission_rate", precision = 5, scale = 2)
    private BigDecimal commissionRate;

    @Column(name = "commission_amount", precision = 15, scale = 2)
    private BigDecimal commissionAmount;

    public enum LeadStatus {
        PENDING, // 待推送
        PUSHED, // 已推送
        ACCEPTED, // 已接受
        REJECTED, // 已拒絕
        APPROVED, // 核准
        DISBURSED, // 已放款
        EXPIRED // 已過期
    }
}

package com.citrus.callect.model;

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
 * 催收案件
 */
@Entity
@Table(name = "collection_cases", indexes = {
        @Index(name = "idx_case_loan", columnList = "loanRecordId"),
        @Index(name = "idx_case_bucket", columnList = "bucket"),
        @Index(name = "idx_case_agent", columnList = "agentId")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionCase extends BaseModel {

    @Column(name = "loan_record_id", nullable = false)
    private Long loanRecordId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "mobile", nullable = false, length = 15)
    private String mobile;

    // ===== DPD 分桶 =====
    @Enumerated(EnumType.STRING)
    @Column(name = "bucket", nullable = false, length = 10)
    private Bucket bucket;

    @Column(name = "dpd", nullable = false)
    private Integer dpd;

    @Column(name = "outstanding_amount", precision = 15, scale = 2)
    private BigDecimal outstandingAmount;

    // ===== 指派 =====
    @Column(name = "agent_id", length = 50)
    private String agentId;

    @Column(name = "agency_code", length = 20)
    private String agencyCode;

    @Column(name = "assigned_date")
    private LocalDate assignedDate;

    // ===== 狀態 =====
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CaseStatus status = CaseStatus.OPEN;

    @Column(name = "ptp_date")
    private LocalDate ptpDate;

    @Column(name = "ptp_amount", precision = 15, scale = 2)
    private BigDecimal ptpAmount;

    @Column(name = "last_contact_date")
    private LocalDate lastContactDate;

    @Column(name = "contact_count")
    @Builder.Default
    private Integer contactCount = 0;

    public enum Bucket {
        B0, // 當期 (DPD = 0)
        B1, // 1-30 天
        B2, // 31-60 天
        B3, // 61-90 天
        NPA // 90+ 天
    }

    public enum CaseStatus {
        OPEN, // 開案
        PTP, // Promise to Pay
        BROKEN_PTP, // 爽約
        RESOLVED, // 已解決
        CLOSED, // 結案
        LEGAL // 轉法務
    }
}

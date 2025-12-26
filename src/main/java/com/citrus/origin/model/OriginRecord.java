package com.citrus.origin.model;

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
 * 審核記錄
 */
@Entity
@Table(name = "origin_records", indexes = {
        @Index(name = "idx_origin_loan", columnList = "loanRecordId"),
        @Index(name = "idx_origin_pan", columnList = "panNumber")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OriginRecord extends BaseModel {

    @Column(name = "loan_record_id", nullable = false)
    private Long loanRecordId;

    @Column(name = "pan_number", nullable = false, length = 10)
    private String panNumber;

    // ===== Dedupe 結果 =====
    @Column(name = "dedupe_pass")
    private Boolean dedupePass;

    @Column(name = "dedupe_reason", length = 200)
    private String dedupeReason;

    // ===== 黑名單結果 =====
    @Column(name = "blacklist_hit")
    private Boolean blacklistHit;

    @Column(name = "blacklist_reason", length = 200)
    private String blacklistReason;

    // ===== 策略引擎結果 =====
    @Column(name = "risk_score")
    private Integer riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_grade", length = 10)
    private RiskGrade riskGrade;

    // ===== 定價結果 =====
    @Column(name = "approved_limit", precision = 15, scale = 2)
    private BigDecimal approvedLimit;

    @Column(name = "roi", precision = 5, scale = 2)
    private BigDecimal roi;

    @Column(name = "tenure")
    private Integer tenure;

    @Column(name = "processing_fee", precision = 10, scale = 2)
    private BigDecimal processingFee;

    // ===== 最終結果 =====
    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 20)
    @Builder.Default
    private Decision decision = Decision.PENDING;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    public enum RiskGrade {
        A, B, C, D, E
    }

    public enum Decision {
        PENDING, // 審核中
        APPROVED, // 通過 (自肥)
        REJECTED, // 拒絕
        LSP // 轉導合作商
    }
}

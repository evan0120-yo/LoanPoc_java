package com.citrus.bureau.model;

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
 * Bureau 查詢快取記錄
 */
@Entity
@Table(name = "bureau_records", indexes = {
        @Index(name = "idx_bureau_pan", columnList = "panNumber"),
        @Index(name = "idx_bureau_type", columnList = "queryType")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BureauRecord extends BaseModel {

    @Column(name = "pan_number", nullable = false, length = 10)
    private String panNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "query_type", nullable = false, length = 20)
    private QueryType queryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private BureauStatus status = BureauStatus.PENDING;

    // ===== PAN 驗證結果 =====
    @Column(name = "pan_valid")
    private Boolean panValid;

    @Column(name = "pan_holder_name", length = 100)
    private String panHolderName;

    @Column(name = "name_match")
    private Boolean nameMatch;

    // ===== CIBIL 結果 =====
    @Column(name = "cibil_score")
    private Integer cibilScore;

    @Column(name = "active_accounts")
    private Integer activeAccounts;

    @Column(name = "overdue_accounts")
    private Integer overdueAccounts;

    @Column(name = "total_outstanding", precision = 15, scale = 2)
    private BigDecimal totalOutstanding;

    // ===== BSA 結果 =====
    @Column(name = "avg_monthly_balance", precision = 15, scale = 2)
    private BigDecimal avgMonthlyBalance;

    @Column(name = "monthly_income", precision = 15, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(name = "foir", precision = 5, scale = 2)
    private BigDecimal foir;

    // ===== 快取控制 =====
    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "raw_response", columnDefinition = "TEXT")
    private String rawResponse;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    public enum QueryType {
        PAN, // PAN 驗證
        CIBIL, // 信用報告
        BSA // 銀行流水分析
    }

    public enum BureauStatus {
        PENDING, // 查詢中
        SUCCESS, // 成功
        FAILED, // 失敗
        CACHED // 使用快取
    }
}

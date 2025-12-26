package com.citrus.origin.object.bo;

import com.citrus.origin.model.OriginRecord.Decision;
import com.citrus.origin.model.OriginRecord.RiskGrade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 審核結果 BO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnderwritingBo {

    private Long loanRecordId;
    private String panNumber;

    // 輸入
    private Integer cibilScore;
    private BigDecimal foir;
    private BigDecimal appliedAmount;

    // 結果
    private Boolean dedupePass;
    private Boolean blacklistHit;
    private Integer riskScore;
    private RiskGrade riskGrade;

    private Decision decision;
    private BigDecimal approvedLimit;
    private BigDecimal roi;
    private Integer tenure;
    private String rejectReason;
}

package com.citrus.loancore.object.resp;

import com.citrus.loancore.model.LoanRecord.LoanState;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 貸款記錄回應
 */
@Data
@Builder
public class LoanRecordResp {

    private Long id;
    private String userId;
    private String mobile;
    private String panNumber;
    private String name;

    private BigDecimal appliedAmount;
    private BigDecimal approvedAmount;
    private BigDecimal disbursedAmount;
    private BigDecimal roi;
    private Integer tenure;
    private BigDecimal emiAmount;

    private LocalDate applicationDate;
    private LocalDate approvalDate;
    private LocalDate disbursalDate;
    private LocalDate maturityDate;

    private LoanState state;
    private String rejectReason;
}

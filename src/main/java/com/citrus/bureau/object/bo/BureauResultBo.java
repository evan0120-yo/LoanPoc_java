package com.citrus.bureau.object.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Bureau 查詢結果 BO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BureauResultBo {

    private String panNumber;

    // PAN 驗證
    private Boolean panValid;
    private String panHolderName;
    private Boolean nameMatch;

    // CIBIL
    private Integer cibilScore;
    private Integer activeAccounts;
    private Integer overdueAccounts;
    private BigDecimal totalOutstanding;

    // BSA
    private BigDecimal avgMonthlyBalance;
    private BigDecimal monthlyIncome;
    private BigDecimal foir;

    // 狀態
    private Boolean completed;
    private String errorMessage;
}

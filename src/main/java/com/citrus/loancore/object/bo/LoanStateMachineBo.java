package com.citrus.loancore.object.bo;

import com.citrus.loancore.model.LoanRecord;
import com.citrus.loancore.model.LoanRecord.LoanState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 貸款狀態機 BO
 * 用於狀態機流轉過程中的資料傳遞
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanStateMachineBo {

    private Long loanRecordId;
    private LoanState currentState;
    private LoanState targetState;

    // Bureau 結果
    private Integer cibilScore;
    private BigDecimal foir;

    // Origin 結果
    private Boolean approved;
    private BigDecimal approvedAmount;
    private BigDecimal roi;
    private Integer tenure;
    private String rejectReason;

    // Sign 結果
    private Boolean signCompleted;

    // Pay 結果
    private String disbursalUtr;
    private BigDecimal disbursedAmount;

    /**
     * 從 LoanRecord 建立 BO
     */
    public static LoanStateMachineBo from(LoanRecord loan) {
        return LoanStateMachineBo.builder()
                .loanRecordId(loan.getId())
                .currentState(loan.getState())
                .approvedAmount(loan.getApprovedAmount())
                .roi(loan.getRoi())
                .tenure(loan.getTenure())
                .disbursalUtr(loan.getDisbursalUtr())
                .disbursedAmount(loan.getDisbursedAmount())
                .build();
    }
}

package com.citrus.loancore.object.req;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanOrderInitReq {
    private String userId;
    private String mobile;
    private String panNumber;
    private String name;
    private BigDecimal appliedAmount;
    private String bankAccount;
    private String ifscCode;
    private String bankName;
}

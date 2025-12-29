package com.citrus.loancore.object.req;

import com.citrus.loancore.enums.LoanStateEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanOrderUpdateReq {
    private String loanOrderId;
    private LoanStateEnum loanState;
    private String triggeredBy;
    private String remark;
}

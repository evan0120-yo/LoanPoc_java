package com.citrus.loancore.usecase.query;

import com.citrus.loancore.model.LoanRecord;
import com.citrus.loancore.object.resp.LoanRecordResp;
import com.citrus.loancore.service.query.LoanRecordQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 查詢貸款記錄 UseCase
 */
@Component
@RequiredArgsConstructor
public class LoanQueryUseCase {

    private final LoanRecordQueryService loanRecordQueryService;

    public LoanRecordResp findById(Long id) {
        LoanRecord loan = loanRecordQueryService.findById(id);
        return toResp(loan);
    }

    public LoanRecordResp findByUserId(String userId) {
        LoanRecord loan = loanRecordQueryService.findByUserId(userId);
        return toResp(loan);
    }

    private LoanRecordResp toResp(LoanRecord loan) {
        return LoanRecordResp.builder()
                .id(loan.getId())
                .userId(loan.getUserId())
                .mobile(loan.getMobile())
                .panNumber(loan.getPanNumber())
                .name(loan.getName())
                .appliedAmount(loan.getAppliedAmount())
                .approvedAmount(loan.getApprovedAmount())
                .disbursedAmount(loan.getDisbursedAmount())
                .roi(loan.getRoi())
                .tenure(loan.getTenure())
                .emiAmount(loan.getEmiAmount())
                .applicationDate(loan.getApplicationDate())
                .approvalDate(loan.getApprovalDate())
                .disbursalDate(loan.getDisbursalDate())
                .maturityDate(loan.getMaturityDate())
                .state(loan.getState())
                .rejectReason(loan.getRejectReason())
                .build();
    }
}

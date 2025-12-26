package com.citrus.loancore.usecase.store;

import com.citrus.common.object.bo.LoanContextBo;
import com.citrus.loancore.model.LoanRecord;
import com.citrus.loancore.model.LoanRecord.LoanState;
import com.citrus.loancore.object.req.LoanApplyReq;
import com.citrus.loancore.service.query.LoanRecordQueryService;
import com.citrus.loancore.service.store.LoanRecordStoreService;
import com.citrus.share.exception.BusinessException;
import com.citrus.share.exception.ErrorCode;
import com.citrus.share.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 貸款申請 UseCase
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoanApplyUseCase {

    private final LoanRecordStoreService loanRecordStoreService;
    private final LoanRecordQueryService loanRecordQueryService;

    /**
     * 執行貸款申請
     */
    @Transactional
    public LoanRecord execute(LoanApplyReq req) {
        log.info("Processing loan application: userId={}, pan={}", req.getUserId(), req.getPanNumber());

        // 1. 檢查是否有進行中的貸款
        if (loanRecordQueryService.hasActiveLoans(req.getPanNumber())) {
            throw new BusinessException(ErrorCode.DUPLICATE_REQUEST, "Active loan already exists");
        }

        // 2. 建立貸款記錄
        LoanRecord loan = LoanRecord.builder()
                .userId(req.getUserId())
                .mobile(req.getMobile())
                .panNumber(req.getPanNumber())
                .name(req.getName())
                .appliedAmount(req.getAppliedAmount())
                .applicationDate(DateTimeUtil.todayIndia())
                .build();

        LoanRecord created = loanRecordStoreService.create(loan);

        log.info("Loan application created: id={}", created.getId());
        return created;
    }

    /**
     * 建立貸款上下文 BO (資料夾模式)
     */
    public LoanContextBo buildContext(LoanRecord loan) {
        return LoanContextBo.builder()
                .loanRecordId(loan.getId())
                .userId(loan.getUserId())
                .mobile(loan.getMobile())
                .panNumber(loan.getPanNumber())
                .currentState(loan.getState().name())
                .build();
    }
}

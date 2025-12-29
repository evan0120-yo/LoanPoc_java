package com.citrus.loancore.usecase.store;

import java.time.Instant;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.citrus.loancore.enums.LoanStateEnum;
import com.citrus.loancore.model.LoanOrder;
import com.citrus.loancore.model.LoanOrderHistory;
import com.citrus.loancore.object.req.LoanOrderInitReq;
import com.citrus.loancore.object.req.LoanOrderUpdateReq;
import com.citrus.loancore.service.guard.LoanOrderGuardService;
import com.citrus.loancore.service.query.LoanOrderQueryService;
import com.citrus.loancore.service.store.LoanOrderStoreService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanOrderStoreUsecase {

    private final LoanOrderStoreService loanOrderStoreService;
    private final LoanOrderQueryService loanOrderQueryService;
    private final LoanOrderGuardService loanOrderGuardService;

    public LoanOrder save(LoanOrderInitReq req) {
        // 1. save order
        Instant now = Instant.now();
        LoanOrder loanOrder = LoanOrder.builder()
                .userId(req.getUserId())
                .mobile(req.getMobile())
                .panNumber(req.getPanNumber())
                .name(req.getName())
                .appliedAmount(req.getAppliedAmount())
                .bankAccount(req.getBankAccount())
                .ifscCode(req.getIfscCode())
                .bankName(req.getBankName())
                .loanState(LoanStateEnum.PENDING)
                .applicationDate(LocalDate.now())
                .createdAt(now)
                .updatedAt(now)
                .build();
        LoanOrder saved = loanOrderStoreService.saveLoanOrder(loanOrder);
        // 2. save history
        LoanOrderHistory loanOrderHistory = LoanOrderHistory.builder()
                .loanOrderId(saved.getLoanOrderId())
                .fromStatus(LoanStateEnum.PENDING)
                .toStatus(LoanStateEnum.PENDING)
                .triggeredBy("loancore")
                .remark("save order")
                .createdAt(now)
                .build();
        loanOrderStoreService.saveLoanOrderHistory(loanOrderHistory);
        return saved;
    }

    public LoanOrder updateState(LoanOrderUpdateReq req) {
        // 1. find by id
        LoanOrder loanOrder = loanOrderQueryService.findById(req.getLoanOrderId());
        // 2. check state
        loanOrderGuardService.checkLoanOrderStatus(loanOrder, req.getLoanState());
        // 3. update state
        LoanOrder updated = loanOrderStoreService.updateState(loanOrder, req.getLoanState());
        // 4. save history
        LoanOrderHistory loanOrderHistory = LoanOrderHistory.builder()
                .loanOrderId(updated.getLoanOrderId())
                .fromStatus(loanOrder.getLoanState())
                .toStatus(req.getLoanState())
                .triggeredBy(req.getTriggeredBy())
                .remark(req.getRemark())
                .createdAt(Instant.now())
                .build();
        loanOrderStoreService.saveLoanOrderHistory(loanOrderHistory);
        return updated;
    }

}

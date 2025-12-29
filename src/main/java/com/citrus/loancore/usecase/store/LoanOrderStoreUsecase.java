package com.citrus.loancore.usecase.store;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.citrus.loancore.enums.LoanStateEnum;
import com.citrus.loancore.model.LoanOrder;
import com.citrus.loancore.object.req.LoanOrderInitReq;
import com.citrus.loancore.service.store.LoanOrderStoreService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanOrderStoreUsecase {

    private final LoanOrderStoreService loanOrderStoreService;

    public LoanOrder save(LoanOrderInitReq req) {
        Instant now = Instant.now();
        LoanOrder loanOrder = LoanOrder.builder()
                .loanOrderId(UUID.randomUUID().toString())
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
        return loanOrderStoreService.save(loanOrder);
    }

    public LoanOrder updateState(LoanOrder loanOrder, LoanStateEnum loanState) {
        return loanOrderStoreService.updateState(loanOrder, loanState);
    }

}

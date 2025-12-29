package com.citrus.loancore.service.guard;

import org.springframework.stereotype.Service;

import com.citrus.loancore.enums.LoanStateEnum;
import com.citrus.loancore.model.LoanOrder;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LoanOrderGuardService {

    private static final Map<LoanStateEnum, Set<LoanStateEnum>> ALLOWED_STATES = Map.ofEntries(
            Map.entry(LoanStateEnum.PENDING, Set.of(LoanStateEnum.BUREAU_CHECK)),
            Map.entry(LoanStateEnum.BUREAU_CHECK, Set.of(LoanStateEnum.UNDERWRITING, LoanStateEnum.REJECTED)),
            Map.entry(LoanStateEnum.UNDERWRITING,
                    Set.of(LoanStateEnum.OFFER_READY, LoanStateEnum.REJECTED, LoanStateEnum.LSP_ROUTING)),
            Map.entry(LoanStateEnum.OFFER_READY, Set.of(LoanStateEnum.SIGN_PENDING)),
            Map.entry(LoanStateEnum.SIGN_PENDING, Set.of(LoanStateEnum.SIGNED, LoanStateEnum.CANCELLED)),
            Map.entry(LoanStateEnum.SIGNED, Set.of(LoanStateEnum.DISBURSAL_PENDING)),
            Map.entry(LoanStateEnum.DISBURSAL_PENDING, Set.of(LoanStateEnum.DISBURSED, LoanStateEnum.DISBURSAL_FAILED)),
            Map.entry(LoanStateEnum.DISBURSAL_FAILED, Set.of(LoanStateEnum.DISBURSAL_PENDING)), // 重試
            Map.entry(LoanStateEnum.DISBURSED, Set.of(LoanStateEnum.ACTIVE)),
            Map.entry(LoanStateEnum.ACTIVE, Set.of(LoanStateEnum.OVERDUE, LoanStateEnum.CLOSED)),
            Map.entry(LoanStateEnum.OVERDUE, Set.of(LoanStateEnum.ACTIVE, LoanStateEnum.NPA, LoanStateEnum.CLOSED)),
            Map.entry(LoanStateEnum.NPA, Set.of(LoanStateEnum.WRITTEN_OFF, LoanStateEnum.CLOSED))
    // REJECTED, LSP_ROUTING, CLOSED, WRITTEN_OFF, CANCELLED 是終態，不允許轉換
    );

    public void checkLoanOrderStatus(LoanOrder loanOrder, LoanStateEnum loanState) {
        LoanStateEnum currentState = loanOrder.getLoanState();
        Set<LoanStateEnum> loanStateEnumSet = ALLOWED_STATES.get(currentState);
        if (loanStateEnumSet == null || !loanStateEnumSet.contains(loanState)) {
            throw new IllegalArgumentException("Invalid state transition from " + currentState + " to " + loanState);
        }
    }
}

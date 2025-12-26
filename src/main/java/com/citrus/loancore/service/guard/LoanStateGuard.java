package com.citrus.loancore.service.guard;

import com.citrus.loancore.model.LoanRecord;
import com.citrus.loancore.model.LoanRecord.LoanState;
import com.citrus.share.exception.BusinessException;
import com.citrus.share.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * 狀態機 Guard - 驗證狀態轉換是否合法
 */
@Component
public class LoanStateGuard {

    /**
     * 合法的狀態轉換表
     */
    private static final Map<LoanState, Set<LoanState>> VALID_TRANSITIONS = Map.ofEntries(
            // 申請階段
            Map.entry(LoanState.PENDING, Set.of(
                    LoanState.BUREAU_CHECK, LoanState.CANCELLED)),
            Map.entry(LoanState.BUREAU_CHECK, Set.of(
                    LoanState.UNDERWRITING, LoanState.REJECTED)),

            // 審核階段
            Map.entry(LoanState.UNDERWRITING, Set.of(
                    LoanState.OFFER_READY, LoanState.REJECTED, LoanState.LSP_ROUTING)),
            Map.entry(LoanState.REJECTED, Set.of()), // 終態
            Map.entry(LoanState.LSP_ROUTING, Set.of()), // 終態 (轉給合作商)

            // 簽約階段
            Map.entry(LoanState.OFFER_READY, Set.of(
                    LoanState.SIGN_PENDING, LoanState.CANCELLED)),
            Map.entry(LoanState.SIGN_PENDING, Set.of(
                    LoanState.SIGNED, LoanState.CANCELLED)),
            Map.entry(LoanState.SIGNED, Set.of(
                    LoanState.DISBURSAL_PENDING)),

            // 放款階段
            Map.entry(LoanState.DISBURSAL_PENDING, Set.of(
                    LoanState.DISBURSED, LoanState.DISBURSAL_FAILED)),
            Map.entry(LoanState.DISBURSAL_FAILED, Set.of(
                    LoanState.DISBURSAL_PENDING, LoanState.CANCELLED)),
            Map.entry(LoanState.DISBURSED, Set.of(
                    LoanState.ACTIVE)),

            // 還款階段
            Map.entry(LoanState.ACTIVE, Set.of(
                    LoanState.OVERDUE, LoanState.CLOSED)),
            Map.entry(LoanState.OVERDUE, Set.of(
                    LoanState.ACTIVE, LoanState.NPA, LoanState.CLOSED)),
            Map.entry(LoanState.NPA, Set.of(
                    LoanState.ACTIVE, LoanState.CLOSED, LoanState.WRITTEN_OFF)),

            // 終態
            Map.entry(LoanState.CLOSED, Set.of()),
            Map.entry(LoanState.WRITTEN_OFF, Set.of()),
            Map.entry(LoanState.CANCELLED, Set.of()));

    /**
     * 驗證狀態轉換是否合法
     */
    public void validateTransition(LoanRecord loan, LoanState targetState) {
        LoanState currentState = loan.getState();

        Set<LoanState> validNextStates = VALID_TRANSITIONS.get(currentState);
        if (validNextStates == null || !validNextStates.contains(targetState)) {
            throw new BusinessException(
                    ErrorCode.LOAN_STATE_INVALID,
                    String.format("Invalid state transition: %s -> %s", currentState, targetState));
        }
    }

    /**
     * 檢查是否為終態
     */
    public boolean isTerminalState(LoanState state) {
        Set<LoanState> nextStates = VALID_TRANSITIONS.get(state);
        return nextStates == null || nextStates.isEmpty();
    }

    /**
     * 檢查是否可取消
     */
    public boolean isCancellable(LoanState state) {
        return VALID_TRANSITIONS.getOrDefault(state, Set.of()).contains(LoanState.CANCELLED);
    }
}

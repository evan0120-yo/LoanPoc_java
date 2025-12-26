package com.citrus.loancore.service.store;

import com.citrus.loancore.model.LoanRecord;
import com.citrus.loancore.model.LoanRecord.LoanState;
import com.citrus.loancore.repository.LoanRecordRepository;
import com.citrus.loancore.service.guard.LoanStateGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * LoanRecord Store Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LoanRecordStoreService {

    private final LoanRecordRepository loanRecordRepository;
    private final LoanStateGuard loanStateGuard;

    /**
     * 建立貸款記錄
     */
    public LoanRecord create(LoanRecord loanRecord) {
        loanRecord.setState(LoanState.PENDING);
        LoanRecord saved = loanRecordRepository.save(loanRecord);
        log.info("LoanRecord created: id={}, userId={}", saved.getId(), saved.getUserId());
        return saved;
    }

    /**
     * 更新貸款記錄
     */
    public LoanRecord update(LoanRecord loanRecord) {
        return loanRecordRepository.save(loanRecord);
    }

    /**
     * 狀態轉換
     */
    public LoanRecord transitState(LoanRecord loan, LoanState targetState) {
        LoanState previousState = loan.getState();

        // Guard 驗證
        loanStateGuard.validateTransition(loan, targetState);

        // 執行狀態轉換
        loan.setState(targetState);
        LoanRecord updated = loanRecordRepository.save(loan);

        log.info("LoanRecord state transition: id={}, {} -> {}",
                loan.getId(), previousState, targetState);

        return updated;
    }

    /**
     * 狀態轉換 (帶原因)
     */
    public LoanRecord transitState(LoanRecord loan, LoanState targetState, String reason) {
        LoanState previousState = loan.getState();

        loanStateGuard.validateTransition(loan, targetState);

        loan.setState(targetState);
        if (targetState == LoanState.REJECTED) {
            loan.setRejectReason(reason);
        }

        LoanRecord updated = loanRecordRepository.save(loan);

        log.info("LoanRecord state transition: id={}, {} -> {}, reason={}",
                loan.getId(), previousState, targetState, reason);

        return updated;
    }
}

package com.citrus.loancore.service.query;

import com.citrus.loancore.model.LoanRecord;
import com.citrus.loancore.model.LoanRecord.LoanState;
import com.citrus.loancore.repository.LoanRecordRepository;
import com.citrus.share.exception.BusinessException;
import com.citrus.share.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * LoanRecord Query Service
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanRecordQueryService {

    private final LoanRecordRepository loanRecordRepository;

    public LoanRecord findById(Long id) {
        return loanRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOAN_NOT_FOUND));
    }

    public LoanRecord findByUserId(String userId) {
        return loanRecordRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOAN_NOT_FOUND));
    }

    public LoanRecord findByMobile(String mobile) {
        return loanRecordRepository.findByMobile(mobile)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOAN_NOT_FOUND));
    }

    public LoanRecord findByPanNumber(String panNumber) {
        return loanRecordRepository.findByPanNumber(panNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOAN_NOT_FOUND));
    }

    public List<LoanRecord> findByState(LoanState state) {
        return loanRecordRepository.findByState(state);
    }

    public List<LoanRecord> findActiveLoans(String userId) {
        return loanRecordRepository.findActiveByUserId(userId);
    }

    public boolean hasActiveLoans(String panNumber) {
        return loanRecordRepository.existsByPanNumberAndStateNotIn(
                panNumber,
                List.of(LoanState.CLOSED, LoanState.WRITTEN_OFF, LoanState.CANCELLED, LoanState.REJECTED));
    }
}

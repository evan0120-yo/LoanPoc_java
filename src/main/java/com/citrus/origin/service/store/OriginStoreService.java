package com.citrus.origin.service.store;

import com.citrus.origin.model.OriginRecord;
import com.citrus.origin.model.OriginRecord.Decision;
import com.citrus.origin.model.OriginRecord.RiskGrade;
import com.citrus.origin.repository.OriginRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Origin Store Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OriginStoreService {

    private final OriginRecordRepository originRecordRepository;

    public OriginRecord create(Long loanRecordId, String panNumber) {
        OriginRecord record = OriginRecord.builder()
                .loanRecordId(loanRecordId)
                .panNumber(panNumber)
                .build();
        return originRecordRepository.save(record);
    }

    public OriginRecord updateDedupeResult(OriginRecord record, boolean pass, String reason) {
        record.setDedupePass(pass);
        record.setDedupeReason(reason);
        return originRecordRepository.save(record);
    }

    public OriginRecord updateBlacklistResult(OriginRecord record, boolean hit, String reason) {
        record.setBlacklistHit(hit);
        record.setBlacklistReason(reason);
        return originRecordRepository.save(record);
    }

    public OriginRecord updateRiskScore(OriginRecord record, int score, RiskGrade grade) {
        record.setRiskScore(score);
        record.setRiskGrade(grade);
        return originRecordRepository.save(record);
    }

    public OriginRecord updatePricing(OriginRecord record, BigDecimal limit, BigDecimal roi, Integer tenure) {
        record.setApprovedLimit(limit);
        record.setRoi(roi);
        record.setTenure(tenure);
        return originRecordRepository.save(record);
    }

    public OriginRecord finalize(OriginRecord record, Decision decision, String rejectReason) {
        record.setDecision(decision);
        record.setRejectReason(rejectReason);
        return originRecordRepository.save(record);
    }
}

package com.citrus.bureau.service.query;

import com.citrus.bureau.model.BureauRecord;
import com.citrus.bureau.model.BureauRecord.QueryType;
import com.citrus.bureau.repository.BureauRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Bureau Query Service
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BureauQueryService {

    private final BureauRecordRepository bureauRecordRepository;

    /**
     * 查找有效的 PAN 快取
     */
    public Optional<BureauRecord> findValidPanCache(String panNumber) {
        return bureauRecordRepository.findValidCache(panNumber, QueryType.PAN, Instant.now());
    }

    /**
     * 查找有效的 CIBIL 快取
     */
    public Optional<BureauRecord> findValidCibilCache(String panNumber) {
        return bureauRecordRepository.findValidCache(panNumber, QueryType.CIBIL, Instant.now());
    }

    /**
     * 查找有效的 BSA 快取
     */
    public Optional<BureauRecord> findValidBsaCache(String panNumber) {
        return bureauRecordRepository.findValidCache(panNumber, QueryType.BSA, Instant.now());
    }

    /**
     * 查找最新記錄
     */
    public Optional<BureauRecord> findLatest(String panNumber, QueryType queryType) {
        return bureauRecordRepository.findTopByPanNumberAndQueryTypeOrderByCreatedAtDesc(panNumber, queryType);
    }
}

package com.citrus.bureau.service.store;

import com.citrus.bureau.model.BureauRecord;
import com.citrus.bureau.model.BureauRecord.BureauStatus;
import com.citrus.bureau.model.BureauRecord.QueryType;
import com.citrus.bureau.repository.BureauRecordRepository;
import com.citrus.share.constant.AppConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Bureau Store Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BureauStoreService {

    private final BureauRecordRepository bureauRecordRepository;

    /**
     * 建立查詢記錄
     */
    public BureauRecord create(String panNumber, QueryType queryType) {
        BureauRecord record = BureauRecord.builder()
                .panNumber(panNumber)
                .queryType(queryType)
                .status(BureauStatus.PENDING)
                .build();
        return bureauRecordRepository.save(record);
    }

    /**
     * 更新為成功
     */
    public BureauRecord markSuccess(BureauRecord record) {
        record.setStatus(BureauStatus.SUCCESS);
        record.setExpiresAt(Instant.now().plus(AppConstant.BUREAU_CACHE_DAYS, ChronoUnit.DAYS));
        return bureauRecordRepository.save(record);
    }

    /**
     * 更新為失敗
     */
    public BureauRecord markFailed(BureauRecord record, String errorMessage) {
        record.setStatus(BureauStatus.FAILED);
        record.setErrorMessage(errorMessage);
        return bureauRecordRepository.save(record);
    }

    /**
     * 標記使用快取
     */
    public BureauRecord markCached(BureauRecord record) {
        record.setStatus(BureauStatus.CACHED);
        return bureauRecordRepository.save(record);
    }
}

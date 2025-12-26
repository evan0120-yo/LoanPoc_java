package com.citrus.bureau.usecase.store;

import com.citrus.bureau.model.BureauRecord;
import com.citrus.bureau.model.BureauRecord.QueryType;
import com.citrus.bureau.object.bo.BureauResultBo;
import com.citrus.bureau.service.guard.BureauGuard;
import com.citrus.bureau.service.query.BureauQueryService;
import com.citrus.bureau.service.store.BureauStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Bureau 查詢 UseCase
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BureauCheckUseCase {

    private final BureauQueryService bureauQueryService;
    private final BureauStoreService bureauStoreService;
    private final BureauGuard bureauGuard;

    /**
     * 執行完整 Bureau 查詢 (PAN + CIBIL + BSA)
     */
    @Transactional
    public BureauResultBo execute(String panNumber, String name) {
        log.info("Starting bureau check for PAN: {}", panNumber);

        // 1. 驗證 PAN 格式
        bureauGuard.validatePanFormat(panNumber);

        BureauResultBo.BureauResultBoBuilder resultBuilder = BureauResultBo.builder()
                .panNumber(panNumber);

        // 2. PAN 驗證 (含快取檢查)
        checkPan(panNumber, name, resultBuilder);

        // 3. CIBIL 查詢 (含快取檢查)
        checkCibil(panNumber, resultBuilder);

        // 4. BSA 分析 (含快取檢查) - 假設已完成
        checkBsa(panNumber, resultBuilder);

        resultBuilder.completed(true);
        BureauResultBo result = resultBuilder.build();

        log.info("Bureau check completed: pan={}, cibil={}, foir={}",
                panNumber, result.getCibilScore(), result.getFoir());

        return result;
    }

    private void checkPan(String panNumber, String name, BureauResultBo.BureauResultBoBuilder builder) {
        Optional<BureauRecord> cache = bureauQueryService.findValidPanCache(panNumber);

        if (cache.isPresent()) {
            BureauRecord cached = cache.get();
            log.debug("Using cached PAN result for: {}", panNumber);
            builder.panValid(cached.getPanValid())
                    .panHolderName(cached.getPanHolderName())
                    .nameMatch(cached.getNameMatch());
            return;
        }

        // TODO: 實際呼叫 PAN API
        // 這裡模擬成功回應
        BureauRecord record = bureauStoreService.create(panNumber, QueryType.PAN);
        record.setPanValid(true);
        record.setPanHolderName(name);
        record.setNameMatch(bureauGuard.isNameMatch(name, name));
        bureauStoreService.markSuccess(record);

        builder.panValid(true)
                .panHolderName(name)
                .nameMatch(true);
    }

    private void checkCibil(String panNumber, BureauResultBo.BureauResultBoBuilder builder) {
        Optional<BureauRecord> cache = bureauQueryService.findValidCibilCache(panNumber);

        if (cache.isPresent()) {
            BureauRecord cached = cache.get();
            log.debug("Using cached CIBIL result for: {}", panNumber);
            builder.cibilScore(cached.getCibilScore())
                    .activeAccounts(cached.getActiveAccounts())
                    .overdueAccounts(cached.getOverdueAccounts())
                    .totalOutstanding(cached.getTotalOutstanding());
            return;
        }

        // TODO: 實際呼叫 CIBIL API
        // 這裡模擬成功回應
        BureauRecord record = bureauStoreService.create(panNumber, QueryType.CIBIL);
        record.setCibilScore(750);
        record.setActiveAccounts(3);
        record.setOverdueAccounts(0);
        record.setTotalOutstanding(new BigDecimal("150000"));
        bureauStoreService.markSuccess(record);

        builder.cibilScore(750)
                .activeAccounts(3)
                .overdueAccounts(0)
                .totalOutstanding(new BigDecimal("150000"));
    }

    private void checkBsa(String panNumber, BureauResultBo.BureauResultBoBuilder builder) {
        Optional<BureauRecord> cache = bureauQueryService.findValidBsaCache(panNumber);

        if (cache.isPresent()) {
            BureauRecord cached = cache.get();
            log.debug("Using cached BSA result for: {}", panNumber);
            builder.avgMonthlyBalance(cached.getAvgMonthlyBalance())
                    .monthlyIncome(cached.getMonthlyIncome())
                    .foir(cached.getFoir());
            return;
        }

        // TODO: 實際呼叫 BSA API
        // 這裡模擬成功回應
        BureauRecord record = bureauStoreService.create(panNumber, QueryType.BSA);
        record.setAvgMonthlyBalance(new BigDecimal("50000"));
        record.setMonthlyIncome(new BigDecimal("80000"));
        record.setFoir(new BigDecimal("0.35"));
        bureauStoreService.markSuccess(record);

        builder.avgMonthlyBalance(new BigDecimal("50000"))
                .monthlyIncome(new BigDecimal("80000"))
                .foir(new BigDecimal("0.35"));
    }
}

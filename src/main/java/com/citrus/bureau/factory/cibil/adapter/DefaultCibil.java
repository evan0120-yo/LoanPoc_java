package com.citrus.bureau.factory.cibil.adapter;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.citrus.bureau.factory.cibil.CibilFactory;
import com.citrus.bureau.factory.cibil.object.bo.CibilResultBo;
import com.citrus.bureau.factory.cibil.object.bo.CibilVerifyBo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultCibil implements CibilFactory {

    @Override
    public CibilResultBo query(CibilVerifyBo input) {
        log.info("DefaultCibil.query() - panNumber: {}", input.getPanNumber());

        // POC: 回傳假資料，全部成功
        return CibilResultBo.builder()
                .isSuccess(true)
                .cibilScore(750)
                .scoreDescription("EXCELLENT")
                .existingLoanCount(2)
                .activeAccounts(3)
                .totalExposure(BigDecimal.valueOf(500000))
                .overdueAmount(BigDecimal.ZERO)
                .hasDefaultHistory(false)
                .lastEnquiryDate(LocalDate.now().minusMonths(1))
                .enquiryCountLast6Months(2)
                .build();
    }
}

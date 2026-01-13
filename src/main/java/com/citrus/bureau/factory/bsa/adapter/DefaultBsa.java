package com.citrus.bureau.factory.bsa.adapter;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.citrus.bureau.factory.bsa.BsaFactory;
import com.citrus.bureau.factory.bsa.object.bo.BsaResultBo;
import com.citrus.bureau.factory.bsa.object.bo.BsaVerifyBo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultBsa implements BsaFactory {

    @Override
    public BsaResultBo analyze(BsaVerifyBo input) {
        log.info("DefaultBsa.analyze() - bankAccountNumber: {}", input.getBankAccountNumber());

        // POC: 回傳假資料，全部成功
        return BsaResultBo.builder()
                .isSuccess(true)
                .monthlyIncome(BigDecimal.valueOf(60000))
                .avgMonthlyBalance(BigDecimal.valueOf(25000))
                .minBalance(BigDecimal.valueOf(5000))
                .salaryRegularity("REGULAR")
                .salaryDay(1)
                .monthsAnalyzed(6)
                .bounceCheckCount(0)
                .emiOutflowCount(2)
                .emiOutflowAmount(BigDecimal.valueOf(15000))
                .build();
    }
}

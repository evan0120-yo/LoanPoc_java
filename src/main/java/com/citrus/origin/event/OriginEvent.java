package com.citrus.origin.event;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.citrus.origin.object.dto.LoanApplyEventDto;
import com.citrus.origin.service.store.OriginOutboxStoreService;
import com.citrus.share.enums.RabbitMQEnum;
import com.fasterxml.uuid.Generators;
import com.google.gson.Gson;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OriginEvent {

    private final OriginOutboxStoreService originOutboxStoreService;
    private final Gson gson;

    public void loanApplyEvent(String userId, String mobile, String panNumber, String name, BigDecimal appliedAmount,
            String bankAccount, String ifscCode, String bankName) {
        log.info("Loan apply event: {}", userId);
        LoanApplyEventDto loanApplyEventDto = LoanApplyEventDto.builder()
                .userId(userId)
                .mobile(mobile)
                .panNumber(panNumber)
                .name(name)
                .appliedAmount(appliedAmount)
                .bankAccount(bankAccount)
                .ifscCode(ifscCode)
                .bankName(bankName)
                .build();
        originOutboxStoreService.save(RabbitMQEnum.ORDER_CREATED.getAggregateType(),
                Generators.timeBasedEpochGenerator().generate().toString(),
                RabbitMQEnum.ORDER_CREATED.name(), gson.toJson(loanApplyEventDto));
    }
}

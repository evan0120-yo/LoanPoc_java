package com.citrus.loancore.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.citrus.origin.object.dto.LoanApplyEventDto;
import com.citrus.share.constants.QueueConstants;
import com.google.gson.Gson;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.citrus.loancore.object.req.LoanOrderInitReq;
import com.citrus.loancore.usecase.store.LoanOrderStoreUsecase;

/**
 * Loancore 訊息消費者
 * 接收來自 Origin 的貸款申請事件
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LoanOrderConsumer {

    private final Gson gson;
    private final LoanOrderStoreUsecase loanOrderStoreUsecase;

    /**
     * 接收訂單創建事件
     * Queue 名稱對應 RabbitMQEnum.ORDER_CREATED 的 queueMap
     */
    @RabbitListener(queues = QueueConstants.LOANCORE_ORDER_CREATED)
    public void handleOrderCreated(String message) {
        log.info("=== Received Order Created Event ===");
        log.info("Raw message: {}", message);

        try {
            LoanApplyEventDto dto = gson.fromJson(message, LoanApplyEventDto.class);
            log.info("Parsed DTO: {}", dto);
            log.info("  userId: {}", dto.getUserId());
            log.info("  mobile: {}", dto.getMobile());
            log.info("  panNumber: {}", dto.getPanNumber());
            log.info("  name: {}", dto.getName());
            log.info("  appliedAmount: {}", dto.getAppliedAmount());
            log.info("  bankAccount: {}", dto.getBankAccount());
            log.info("  ifscCode: {}", dto.getIfscCode());
            log.info("  bankName: {}", dto.getBankName());

            LoanOrderInitReq loanOrderInitReq = LoanOrderInitReq.builder()
                    .userId(dto.getUserId())
                    .mobile(dto.getMobile())
                    .panNumber(dto.getPanNumber())
                    .name(dto.getName())
                    .appliedAmount(dto.getAppliedAmount())
                    .bankAccount(dto.getBankAccount())
                    .ifscCode(dto.getIfscCode())
                    .bankName(dto.getBankName())
                    .build();
            loanOrderStoreUsecase.save(loanOrderInitReq);
        } catch (Exception e) {
            log.error("Failed to parse message", e);
        }

        log.info("=== End of Event Processing ===");
    }
}

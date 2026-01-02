package com.citrus.loancore.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.citrus.loancron.object.dto.ReviewOrderEventDto;
import com.citrus.share.constants.QueueConstants;
import com.google.gson.Gson;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 審核訂單 Consumer
 * 接收來自 loancron 的審核請求
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewOrderConsumer {

    private final Gson gson;
    // private final ReviewOrderUsecase reviewOrderUsecase; // TODO: 之後實作

    @RabbitListener(queues = QueueConstants.LOANCORE_REVIEW_ORDER)
    public void handleReviewOrder(String message) {
        log.info("=== Received Review Order Event ===");
        log.info("Raw message: {}", message);

        try {
            ReviewOrderEventDto dto = gson.fromJson(message, ReviewOrderEventDto.class);
            log.info("Parsed DTO: {}", dto);
            log.info("  orderId: {}", dto.getOrderId());
            log.info("  triggerAt: {}", dto.getTriggerAt());

            // TODO: 觸發審核流程
            // reviewOrderUsecase.review(dto.getOrderId());

            log.info("Review order processing completed for orderId: {}", dto.getOrderId());

        } catch (Exception e) {
            log.error("Failed to process review order message", e);
        }

        log.info("=== End of Review Order Processing ===");
    }
}

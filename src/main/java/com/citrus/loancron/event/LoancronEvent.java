package com.citrus.loancron.event;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.citrus.loancron.object.dto.ReviewOrderEventDto;
import com.citrus.loancron.service.store.LoancronOutboxStoreService;
import com.citrus.share.enums.RabbitMQEnum;
import com.google.gson.Gson;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Loancron 事件發送器
 * 將事件寫入 Outbox，由排程服務發送到 MQ
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LoancronEvent {

    private final LoancronOutboxStoreService outboxStoreService;
    private final Gson gson;

    /**
     * 發送審核訂單事件
     * 
     * @param orderId 訂單 ID
     */
    public void reviewOrderEvent(String orderId) {
        log.info("Sending review order event for orderId: {}", orderId);

        ReviewOrderEventDto dto = ReviewOrderEventDto.builder()
                .orderId(orderId)
                .triggerAt(Instant.now())
                .build();

        outboxStoreService.save(
                RabbitMQEnum.REVIEW_ORDER.getAggregateType(),
                orderId,
                RabbitMQEnum.REVIEW_ORDER.name(),
                gson.toJson(dto));

        log.info("Review order event saved to outbox for orderId: {}", orderId);
    }
}

package com.citrus.loancron.event;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.citrus.loancron.service.store.LoancronOutboxStoreService;
import com.citrus.share.enums.RabbitMQEnum;
import com.google.gson.Gson;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Loancron 事件發送器
 * 只負責發送觸發訊號，不攜帶業務資料
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LoancronEvent {

    private final LoancronOutboxStoreService outboxStoreService;
    private final Gson gson;

    /**
     * 發送「開始審核 PENDING 訂單」觸發訊號
     * 不包含任何 orderId，loancore 收到後自己查詢
     */
    public void triggerReviewEvent() {
        log.info("Sending trigger review event");

        // 只發送觸發時間，不帶業務資料
        TriggerEventPayload payload = new TriggerEventPayload(Instant.now());

        outboxStoreService.save(
                RabbitMQEnum.REVIEW_ORDER.getAggregateType(),
                "TRIGGER", // aggregateId 用固定值
                RabbitMQEnum.REVIEW_ORDER.name(),
                gson.toJson(payload));

        log.info("Trigger review event saved to outbox");
    }

    /**
     * 簡單的觸發訊號 payload
     */
    private record TriggerEventPayload(Instant triggerAt) {
    }
}

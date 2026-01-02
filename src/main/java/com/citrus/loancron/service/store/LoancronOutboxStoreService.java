package com.citrus.loancron.service.store;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.citrus.common.enums.OutboxStatusEnum;
import com.citrus.loancron.dao.LoancronOutboxDao;
import com.citrus.loancron.model.LoancronOutbox;
import com.citrus.share.enums.RabbitMQEnum;
import com.fasterxml.uuid.Generators;

import lombok.RequiredArgsConstructor;

/**
 * Loancron Outbox Store Service
 * 將事件寫入 Outbox 表
 */
@Service
@RequiredArgsConstructor
public class LoancronOutboxStoreService {

    private final LoancronOutboxDao outboxDao;

    public LoancronOutbox save(String aggregateType, String aggregateId, String eventType, String payload) {
        LoancronOutbox outbox = new LoancronOutbox();
        outbox.setOutboxId(Generators.timeBasedEpochGenerator().generate().toString());
        outbox.setAggregateType(aggregateType);
        outbox.setAggregateId(aggregateId);
        outbox.setEventType(eventType);
        outbox.setTargetExchange(RabbitMQEnum.REVIEW_ORDER.getExchangeName());
        outbox.setTargetRoutingKey(RabbitMQEnum.REVIEW_ORDER.getRoutingKey());
        outbox.setPayload(payload);
        outbox.setStatus(OutboxStatusEnum.PENDING);
        outbox.setRetryCount(0);
        outbox.setCreatedAt(Instant.now());

        return outboxDao.save(outbox);
    }
}

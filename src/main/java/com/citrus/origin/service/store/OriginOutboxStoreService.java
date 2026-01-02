package com.citrus.origin.service.store;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import com.citrus.common.enums.OutboxStatusEnum;
import com.citrus.origin.dao.OriginOutboxDao;
import com.citrus.origin.model.OriginOutbox;
import com.citrus.share.enums.RabbitMQEnum;

@Service
@RequiredArgsConstructor
public class OriginOutboxStoreService {

    private final OriginOutboxDao outboxDao;

    public OriginOutbox save(String aggregateType, String aggregateId, String eventType, String payload) {
        OriginOutbox outbox = new OriginOutbox();
        outbox.setAggregateType(aggregateType);
        outbox.setAggregateId(aggregateId);
        outbox.setEventType(eventType);
        outbox.setTargetExchange(RabbitMQEnum.ORDER_CREATED.getExchangeName());
        outbox.setTargetRoutingKey(RabbitMQEnum.ORDER_CREATED.getRoutingKey());
        outbox.setPayload(payload);
        outbox.setStatus(OutboxStatusEnum.PENDING);
        outbox.setRetryCount(0);
        return outboxDao.save(outbox);
    }
}

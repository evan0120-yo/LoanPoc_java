package com.citrus.loancore.service.store;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.citrus.common.enums.OutboxStatusEnum;
import com.citrus.loancore.dao.LoancoreOutboxDao;
import com.citrus.loancore.model.LoancoreOutbox;
import com.citrus.share.enums.RabbitMQEnum;
import com.fasterxml.uuid.Generators;

import lombok.RequiredArgsConstructor;

/**
 * Loancore Outbox Store Service
 * 將事件寫入 Outbox 表
 */
@Service
@RequiredArgsConstructor
public class LoancoreOutboxStoreService {

    private final LoancoreOutboxDao outboxDao;

    public LoancoreOutbox save(String aggregateType, String aggregateId, String eventType, String payload) {
        LoancoreOutbox outbox = createOutbox(aggregateType, aggregateId, eventType, payload);
        return outboxDao.save(outbox);
    }

    /**
     * 批次儲存 Outbox 訊息（減少 DB round-trip）
     */
    public List<LoancoreOutbox> saveAll(List<LoancoreOutbox> outboxList) {
        return outboxDao.saveAll(outboxList);
    }

    /**
     * 建立 Outbox 訊息物件（不儲存）
     */
    public LoancoreOutbox createOutbox(String aggregateType, String aggregateId, String eventType, String payload) {
        LoancoreOutbox outbox = new LoancoreOutbox();
        outbox.setOutboxId(Generators.timeBasedEpochGenerator().generate().toString());
        outbox.setAggregateType(aggregateType);
        outbox.setAggregateId(aggregateId);
        outbox.setEventType(eventType);
        outbox.setTargetExchange(RabbitMQEnum.PENDING_ORDER.getExchangeName());
        outbox.setTargetRoutingKey(RabbitMQEnum.PENDING_ORDER.getRoutingKey());
        outbox.setPayload(payload);
        outbox.setStatus(OutboxStatusEnum.PENDING);
        outbox.setRetryCount(0);
        outbox.setCreatedAt(Instant.now());
        return outbox;
    }
}

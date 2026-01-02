package com.citrus.loancron.service.schedule;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.citrus.common.repository.OutboxRepository;
import com.citrus.common.service.OutboxScheduleService;
import com.citrus.loancron.model.LoancronOutbox;
import com.citrus.loancron.repository.LoancronOutboxRepository;
import com.fasterxml.uuid.Generators;

import lombok.RequiredArgsConstructor;

/**
 * Loancron Outbox 排程發送服務
 * 定時掃描 Outbox 並發送到 RabbitMQ
 */
@Service
@RequiredArgsConstructor
public class LoancronOutboxScheduleService extends OutboxScheduleService<LoancronOutbox> {

    private final LoancronOutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    private final String processorId = Generators.timeBasedEpochGenerator().generate().toString();

    @Override
    protected OutboxRepository<LoancronOutbox> getOutboxRepository() {
        return outboxRepository;
    }

    @Override
    protected String getProcessorId() {
        return processorId;
    }

    @Override
    protected void sendMessage(LoancronOutbox message) throws Exception {
        rabbitTemplate.convertAndSend(
                message.getTargetExchange(),
                message.getTargetRoutingKey(),
                message.getPayload());
    }
}

package com.citrus.loancore.service.schedule;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.citrus.common.repository.OutboxRepository;
import com.citrus.common.service.OutboxScheduleService;
import com.citrus.loancore.model.LoancoreOutbox;
import com.citrus.loancore.repository.LoancoreOutboxRepository;
import com.fasterxml.uuid.Generators;

import lombok.RequiredArgsConstructor;

/**
 * Loancore Outbox 排程發送服務
 * 定時掃描 Outbox 並發送到 RabbitMQ
 */
@Service
@RequiredArgsConstructor
public class LoancoreOutboxScheduleService extends OutboxScheduleService<LoancoreOutbox> {

    private final LoancoreOutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    private final String processorId = Generators.timeBasedEpochGenerator().generate().toString();

    @Override
    protected OutboxRepository<LoancoreOutbox> getOutboxRepository() {
        return outboxRepository;
    }

    @Override
    protected String getProcessorId() {
        return processorId;
    }

    @Override
    protected void sendMessage(LoancoreOutbox message) throws Exception {
        rabbitTemplate.convertAndSend(
                message.getTargetExchange(),
                message.getTargetRoutingKey(),
                message.getPayload());
    }
}

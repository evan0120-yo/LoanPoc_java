package com.citrus.origin.service.schedule;

import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.citrus.common.repository.OutboxRepository;
import com.citrus.common.service.OutboxScheduleService;
import com.citrus.origin.model.OriginOutbox;
import com.citrus.origin.repository.OriginOutboxRepository;
import com.fasterxml.uuid.Generators;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OriginOutboxScheduleService extends OutboxScheduleService<OriginOutbox> {

    private final OriginOutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    private final String processorId = Generators.timeBasedEpochGenerator().generate().toString();

    @Override
    protected OutboxRepository<OriginOutbox> getOutboxRepository() {
        return outboxRepository;
    }

    @Override
    protected String getProcessorId() {
        return processorId;
    }

    @Override
    protected void sendMessage(OriginOutbox message) throws Exception {
        rabbitTemplate.convertAndSend(message.getTargetExchange(), message.getTargetRoutingKey(), message.getPayload());
    }
}

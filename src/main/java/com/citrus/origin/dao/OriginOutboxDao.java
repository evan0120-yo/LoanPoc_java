package com.citrus.origin.dao;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.citrus.origin.model.OriginOutbox;
import com.citrus.origin.repository.OriginOutboxRepository;
import com.fasterxml.uuid.Generators;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OriginOutboxDao {

    private final OriginOutboxRepository originOutboxRepository;

    public OriginOutbox save(OriginOutbox outbox) {
        outbox.setOutboxId(Generators.defaultTimeBasedGenerator().generate().toString());
        outbox.setCreatedAt(Instant.now());
        return originOutboxRepository.save(outbox);
    }

    public OriginOutbox findById(String id) {
        return originOutboxRepository.findById(id).orElse(null);
    }
}

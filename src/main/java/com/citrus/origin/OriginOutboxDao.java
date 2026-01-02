package com.citrus.origin;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.citrus.origin.model.OriginOutbox;
import com.citrus.origin.repository.OriginOutboxRepository;

@Component
@RequiredArgsConstructor
public class OriginOutboxDao {

    private final OriginOutboxRepository originOutboxRepository;

    public OriginOutbox save(OriginOutbox originOutbox) {
        return originOutboxRepository.save(originOutbox);
    }

    public OriginOutbox findById(String id) {
        return originOutboxRepository.findById(id).orElse(null);
    }
}

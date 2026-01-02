package com.citrus.loancron.dao;

import org.springframework.stereotype.Repository;

import com.citrus.loancron.model.LoancronOutbox;
import com.citrus.loancron.repository.LoancronOutboxRepository;

import lombok.RequiredArgsConstructor;

/**
 * Loancron Outbox Dao
 */
@Repository
@RequiredArgsConstructor
public class LoancronOutboxDao {

    private final LoancronOutboxRepository repository;

    public LoancronOutbox save(LoancronOutbox outbox) {
        return repository.save(outbox);
    }
}

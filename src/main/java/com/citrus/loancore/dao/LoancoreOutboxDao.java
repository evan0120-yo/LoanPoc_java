package com.citrus.loancore.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.citrus.loancore.model.LoancoreOutbox;
import com.citrus.loancore.repository.LoancoreOutboxRepository;

import lombok.RequiredArgsConstructor;

/**
 * Loancore Outbox Dao
 */
@Repository
@RequiredArgsConstructor
public class LoancoreOutboxDao {

    private final LoancoreOutboxRepository repository;

    public LoancoreOutbox save(LoancoreOutbox outbox) {
        return repository.save(outbox);
    }

    /**
     * 批次儲存 Outbox 訊息（減少 DB round-trip）
     */
    public List<LoancoreOutbox> saveAll(List<LoancoreOutbox> outboxList) {
        return repository.saveAll(outboxList);
    }
}

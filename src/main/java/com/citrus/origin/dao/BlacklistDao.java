package com.citrus.origin.dao;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.citrus.origin.model.Blacklist;
import com.citrus.origin.repository.BlacklistRepository;
import com.fasterxml.uuid.Generators;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BlacklistDao {

    private final BlacklistRepository blacklistRepository;

    public Blacklist save(Blacklist blacklist) {
        Instant now = Instant.now();
        blacklist.setBlacklistId(Generators.defaultTimeBasedGenerator().generate().toString());
        blacklist.setCreatedAt(now);
        return blacklistRepository.save(blacklist);
    }

    public void delete(Blacklist blacklist, String deletedBy) {
        Instant now = Instant.now();
        blacklist.setDeletedAt(now);
        blacklist.setDeletedBy(deletedBy);
        blacklistRepository.save(blacklist);
    }

    public Optional<Blacklist> findById(String id) {
        return blacklistRepository.findById(id);
    }

    public List<Blacklist> findByExistUser(String userId) {
        return blacklistRepository.findByUserIdAndDeletedAt(userId, null);
    }

    public List<Blacklist> findByUserId(String userId) {
        return blacklistRepository.findByUserId(userId);
    }

}

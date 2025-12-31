package com.citrus.origin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

import com.citrus.origin.model.Blacklist;

public interface BlacklistRepository extends JpaRepository<Blacklist, String> {
    List<Blacklist> findByUserId(String userId);

    List<Blacklist> findByUserIdAndDeletedAt(String userId, Instant deletedAt);
}

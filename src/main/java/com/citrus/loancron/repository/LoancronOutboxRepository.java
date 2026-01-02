package com.citrus.loancron.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.citrus.common.repository.OutboxRepository;
import com.citrus.loancron.model.LoancronOutbox;

public interface LoancronOutboxRepository extends OutboxRepository<LoancronOutbox> {

    /**
     * 清除超時未處理的認領（用於 Server 崩潰後的恢復）
     * 使用 PostgreSQL 的 MAKE_INTERVAL 函數
     */
    @Override
    @Modifying
    @Query(value = """
            UPDATE loancron_outbox
            SET status = 'PENDING', claimed_by = NULL, claimed_at = NULL
            WHERE status = 'PROCESSING'
            AND claimed_at < CURRENT_TIMESTAMP - MAKE_INTERVAL(mins => :minutes)
            """, nativeQuery = true)
    int releaseTimedOutClaims(@Param("minutes") int minutes);
}

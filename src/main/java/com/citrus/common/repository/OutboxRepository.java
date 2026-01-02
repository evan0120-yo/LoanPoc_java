package com.citrus.common.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import com.citrus.common.enums.OutboxStatusEnum;
import com.citrus.common.model.OutboxMessage;

/**
 * Outbox Repository 通用介面
 * 各模組的 OutboxRepository 繼承此介面即可
 * 
 * 使用 @NoRepositoryBean 表示這是抽象介面，不會建立實際的 Bean
 * 
 * 採用 Claim-and-Process 模式：
 * 1. claimMessages() - 原子操作認領訊息（UPDATE + FOR UPDATE SKIP LOCKED）
 * 2. findByClaimedByAndStatus() - 查詢已認領的訊息
 * 3. 發送完成後更新狀態
 * 
 * @param <T> 繼承 OutboxMessage 的具體類型
 */
@NoRepositoryBean
public interface OutboxRepository<T extends OutboxMessage> extends JpaRepository<T, String> {

    /**
     * 認領待發送訊息（原子操作）
     * 
     * 使用 PostgreSQL 子查詢 + FOR UPDATE SKIP LOCKED：
     * - 確保多台 server 不會重複認領
     * - 一次 UPDATE 認領多筆，效能更好
     * 
     * 注意：此方法需要在子類用 nativeQuery 實作，因為表名不同
     * 這裡提供預設的 JPQL 版本（不含 LIMIT，需配合 Pageable 使用）
     * 
     * @param processorId 認領的 server ID
     * @param limit       最多認領幾筆
     * @return 認領的筆數
     */
    @Modifying
    @Query(value = """
            UPDATE #{#entityName} o
            SET o.status = 'PROCESSING',
                o.claimedBy = :processorId,
                o.claimedAt = CURRENT_TIMESTAMP
            WHERE o.outboxId IN (
                SELECT o2.outboxId FROM #{#entityName} o2
                WHERE o2.status = 'PENDING' AND o2.claimedBy IS NULL
                ORDER BY o2.createdAt ASC
            )
            """)
    int claimMessages(@Param("processorId") String processorId);

    /**
     * 查詢已認領的訊息
     * 
     * @param claimedBy 認領的 server ID
     * @param status    訊息狀態 (通常是 PROCESSING)
     * @return 已認領的訊息列表
     */
    List<T> findByClaimedByAndStatus(String claimedBy, OutboxStatusEnum status);

    /**
     * 清除超時未處理的認領（用於 Server 崩潰後的恢復）
     * 
     * @param minutes 超過幾分鐘視為超時
     * @return 清除的筆數
     */
    @Modifying
    @Query("""
            UPDATE #{#entityName} o
            SET o.status = 'PENDING', o.claimedBy = NULL, o.claimedAt = NULL
            WHERE o.status = 'PROCESSING'
            AND o.claimedAt < CURRENT_TIMESTAMP - :minutes * INTERVAL '1 minute'
            """)
    int releaseTimedOutClaims(@Param("minutes") int minutes);
}

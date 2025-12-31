package com.citrus.common.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import com.citrus.common.enums.OutboxStatusEnum;
import com.citrus.common.model.OutboxMessage;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

/**
 * Outbox Repository 通用介面
 * 各模組的 OutboxRepository 繼承此介面即可
 * 
 * 使用 @NoRepositoryBean 表示這是抽象介面，不會建立實際的 Bean
 * 
 * @param <T> 繼承 OutboxMessage 的具體類型
 */
@NoRepositoryBean
public interface OutboxRepository<T extends OutboxMessage> extends JpaRepository<T, String> {

    /**
     * 查詢待發送訊息（使用悲觀鎖 + SKIP LOCKED）
     * 
     * 使用方式：
     * - PESSIMISTIC_WRITE: 相當於 FOR UPDATE
     * - lock.timeout = 0: 相當於 SKIP LOCKED (PostgreSQL)
     * - #{#entityName}: 自動替換成子類的實體名稱
     * 
     * 這樣可以確保多台 server 不會重複處理相同訊息
     * 
     * @param status   訊息狀態 (通常是 PENDING)
     * @param pageable 分頁參數 (用來限制數量)
     * @return 被鎖定的訊息列表
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({ @QueryHint(name = "javax.persistence.lock.timeout", value = "0") })
    @Query("SELECT o FROM #{#entityName} o WHERE o.status = :status ORDER BY o.createdAt ASC")
    List<T> findByStatusForUpdate(
            @Param("status") OutboxStatusEnum status,
            Pageable pageable);
}

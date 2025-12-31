package com.citrus.common.worker;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;

import com.citrus.common.enums.OutboxStatusEnum;
import com.citrus.common.model.OutboxMessage;
import com.citrus.common.repository.OutboxRepository;
import com.citrus.common.service.OutboxService;

import lombok.extern.slf4j.Slf4j;

/**
 * Outbox Worker 抽象基類
 * 各模組繼承此類實作自己的 Outbox Worker
 * 
 * 職責：
 * 1. 定時掃描 PENDING 訊息
 * 2. 呼叫子類實作的 sendMessage() 發送到 MQ
 * 3. 更新訊息狀態 (SENT / FAILED)
 * 4. 處理重試邏輯
 * 
 * @param <T> 繼承 OutboxMessage 的具體類型
 */
@Slf4j
public abstract class OutboxWorkerBase<T extends OutboxMessage> {

    /**
     * 子類必須提供 OutboxService
     */
    protected abstract OutboxService<T> getOutboxService();

    /**
     * 子類必須提供 OutboxRepository
     */
    protected abstract OutboxRepository<T> getOutboxRepository();

    /**
     * 子類必須實作發送邏輯
     * 
     * @param message Outbox 訊息
     * @throws Exception 發送失敗時拋出異常
     */
    protected abstract void sendMessage(T message) throws Exception;

    /**
     * 子類可以自訂最大重試次數 (預設 3 次)
     */
    protected int getMaxRetryCount() {
        return 3;
    }

    /**
     * 子類可以自訂每次處理的訊息數量 (預設 100 筆)
     */
    protected int getBatchSize() {
        return 100;
    }

    /**
     * 定時處理 Outbox 訊息
     * 
     * 預設每 5 秒執行一次
     * 子類可以覆寫此方法自訂排程
     */
    @Scheduled(fixedDelay = 5000)
    public void processMessages() {
        OutboxService<T> outboxService = getOutboxService();

        // 使用 Repository 的鎖定查詢（FOR UPDATE SKIP LOCKED）
        // 確保多台 server 不會重複處理相同訊息
        List<T> pendingMessages = getOutboxRepository().findByStatusForUpdate(
                OutboxStatusEnum.PENDING,
                PageRequest.of(0, getBatchSize()));

        if (pendingMessages.isEmpty()) {
            return;
        }

        log.info("Processing {} pending outbox messages", pendingMessages.size());

        for (T message : pendingMessages) {
            try {
                // 發送訊息（由子類實作）
                sendMessage(message);

                // 標記為已發送
                outboxService.markAsSent(message.getOutboxId());
                log.info("Successfully sent outbox message: {}", message.getOutboxId());

            } catch (Exception e) {
                log.error("Failed to send outbox message: {}", message.getOutboxId(), e);

                // 增加重試次數
                outboxService.incrementRetryCount(message.getOutboxId());

                // 檢查是否超過最大重試次數
                if (message.getRetryCount() + 1 >= getMaxRetryCount()) {
                    outboxService.markAsFailed(message.getOutboxId(), e.getMessage());
                    log.error("Outbox message {} marked as FAILED after {} retries",
                            message.getOutboxId(), getMaxRetryCount());
                }
            }
        }
    }
}

package com.citrus.common.service;

import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import com.citrus.common.enums.OutboxStatusEnum;
import com.citrus.common.model.OutboxMessage;
import com.citrus.common.repository.OutboxRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Outbox 排程發送服務基類
 * 各模組繼承此類放在 service/schedule/ 路徑
 * 
 * 採用 Claim-and-Process 模式：
 * 1. claimMessages() - 原子操作認領訊息
 * 2. findByClaimedByAndStatus() - 查詢已認領的訊息
 * 3. 發送並更新狀態
 * 
 * 優點：
 * - 一次 UPDATE 認領多筆，效能更好
 * - 使用 processorId 防止多台 server 重複處理
 * - 支援 server 崩潰後的訊息恢復
 * 
 * @param <T> 繼承 OutboxMessage 的具體類型
 */
@Slf4j
public abstract class OutboxScheduleService<T extends OutboxMessage> {

    /**
     * 子類必須提供 OutboxRepository
     */
    protected abstract OutboxRepository<T> getOutboxRepository();

    /**
     * 子類必須提供 Processor ID（用於識別是哪台 server 認領）
     * 例如：從 application.properties 取得 outbox.processor-id
     */
    protected abstract String getProcessorId();

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
     * 定時處理 Outbox 訊息
     * 
     * 採用 Claim-and-Process 模式：
     * 1. 先用 UPDATE 認領訊息（原子操作，含 FOR UPDATE SKIP LOCKED）
     * 2. 再 SELECT 已認領的訊息
     * 3. 發送並更新狀態
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processMessages() {
        OutboxRepository<T> repository = getOutboxRepository();
        String processorId = getProcessorId();

        // Step 1: Claim - 認領待發送訊息（原子操作）
        int claimedCount = repository.claimMessages(processorId);
        if (claimedCount == 0) {
            return;
        }

        log.info("Claimed {} outbox messages with processorId: {}", claimedCount, processorId);

        // Step 2: Find - 查詢已認領的訊息
        List<T> claimedMessages = repository.findByClaimedByAndStatus(
                processorId, OutboxStatusEnum.PROCESSING);

        // Step 3: Send and Update
        for (T message : claimedMessages) {
            try {
                // 發送訊息（由子類實作）
                sendMessage(message);

                // 標記為已發送
                message.setStatus(OutboxStatusEnum.SENT);
                message.setSentAt(Instant.now());
                repository.save(message);

                log.info("Successfully sent outbox message: {}", message.getOutboxId());

            } catch (Exception e) {
                log.error("Failed to send outbox message: {}", message.getOutboxId(), e);

                // 增加重試次數
                message.setRetryCount(message.getRetryCount() + 1);

                // 檢查是否超過最大重試次數
                if (message.getRetryCount() >= getMaxRetryCount()) {
                    message.setStatus(OutboxStatusEnum.FAILED);
                    message.setErrorMessage(e.getMessage());
                    log.error("Outbox message {} marked as FAILED after {} retries",
                            message.getOutboxId(), getMaxRetryCount());
                } else {
                    // 放回 PENDING 狀態，等待下次重試
                    message.setStatus(OutboxStatusEnum.PENDING);
                    message.setClaimedBy(null);
                    message.setClaimedAt(null);
                }

                repository.save(message);
            }
        }
    }

    /**
     * 定時清理超時的認領（可選）
     * 
     * 當 server 崩潰後，其認領的訊息會卡在 PROCESSING 狀態
     * 這個方法會將超時的訊息放回 PENDING 狀態
     */
    @Scheduled(fixedDelay = 60000) // 每分鐘執行一次
    @Transactional
    public void releaseTimedOutClaims() {
        int released = getOutboxRepository().releaseTimedOutClaims(5); // 5 分鐘超時
        if (released > 0) {
            log.warn("Released {} timed out claims", released);
        }
    }
}

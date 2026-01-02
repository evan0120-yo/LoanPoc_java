package com.citrus.loancron.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.citrus.loancron.event.LoancronEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

/**
 * 待審批訂單排程器
 * 
 * 只負責定時發送「開始審核」訊號給 loancore
 * loancore 收到後會自己查詢並處理 PENDING 訂單
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PendingOrderScheduler {

    private final LoancronEvent loancronEvent;

    /**
     * 每 20 秒發送「開始審核」訊號（測試用，生產改回 5 分鐘）
     * 
     * @SchedulerLock 確保多台 Server 只有一台執行
     */
    @Scheduled(fixedDelay = 20000) // 每 20 秒（測試用，生產改回 5 分鐘）
    @SchedulerLock(name = "processPendingOrders", lockAtLeastFor = "15s", lockAtMostFor = "20s")
    public void triggerPendingOrderReview() {
        log.info("=== Trigger Pending Order Review ===");

        // 只發送觸發訊號，不查詢任何 loancore 資料
        loancronEvent.triggerReviewEvent();

        log.info("=== Trigger Sent ===");
    }
}

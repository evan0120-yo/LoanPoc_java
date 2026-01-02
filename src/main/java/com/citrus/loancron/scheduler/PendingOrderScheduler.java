package com.citrus.loancron.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.citrus.loancron.event.LoancronEvent;
import com.citrus.loancron.service.query.LoancronQueryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

/**
 * 待審批訂單排程器
 * 
 * 每 5 分鐘掃描 PENDING 狀態的訂單，發送 MQ 讓 loancore 處理
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PendingOrderScheduler {

    private final LoancronQueryService queryService;
    private final LoancronEvent loancronEvent;

    /**
     * 每 5 分鐘處理 PENDING 訂單
     * 
     * @SchedulerLock 確保多台 Server 只有一台執行：
     *                - name: 鎖的名稱（唯一）
     *                - lockAtLeastFor: 最少鎖多久（防止執行太快重複觸發）
     *                - lockAtMostFor: 最多鎖多久（防止 Server 崩潰鎖永遠不釋放）
     */
    @Scheduled(fixedDelay = 300000) // 每 5 分鐘
    @SchedulerLock(name = "processPendingOrders", lockAtLeastFor = "4m", lockAtMostFor = "5m")
    public void processPendingOrders() {
        log.info("=== Start Processing Pending Orders ===");

        List<String> pendingOrderIds = queryService.findPendingOrderIds();

        if (pendingOrderIds.isEmpty()) {
            log.info("No pending orders found");
            return;
        }

        log.info("Found {} pending orders to review", pendingOrderIds.size());

        for (String orderId : pendingOrderIds) {
            loancronEvent.reviewOrderEvent(orderId);
        }

        log.info("=== End Processing Pending Orders ===");
    }
}

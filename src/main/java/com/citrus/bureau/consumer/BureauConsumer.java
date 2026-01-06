package com.citrus.bureau.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.citrus.loancore.object.dto.PendingOrderDto;
import com.citrus.share.constants.QueueConstants;
import com.google.gson.Gson;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Bureau Consumer
 * 接收來自 Loancore 的待審核訂單，執行徵信查詢
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BureauConsumer {

    private final Gson gson;

    @RabbitListener(queues = QueueConstants.BUREAU_PENDING_ORDER)
    public void handlePendingOrder(String message) {
        log.info("=== Bureau Received Pending Order ===");
        log.info("Raw message: {}", message);

        try {
            // 解析 JSON
            PendingOrderDto payload = gson.fromJson(message, PendingOrderDto.class);

            log.info("Parsed order:");
            log.info("  - loanOrderId: {}", payload.getLoanOrderId());
            log.info("  - userId: {}", payload.getUserId());
            log.info("  - panNumber: {}", payload.getPanNumber());
            log.info("  - name: {}", payload.getName());
            log.info("  - appliedAmount: {}", payload.getAppliedAmount());

            // TODO: 執行徵信查詢
            // 1. PAN 驗證
            // 2. CIBIL 查詢
            // 3. BSA 分析

        } catch (Exception e) {
            log.error("Failed to parse pending order message: {}", e.getMessage(), e);
        }

        log.info("=== End of Bureau Processing ===");
    }
}

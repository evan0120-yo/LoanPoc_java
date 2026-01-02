package com.citrus.loancore.consumer;

import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.citrus.loancore.enums.LoanStateEnum;
import com.citrus.loancore.model.LoanOrder;
import com.citrus.loancore.repository.LoanOrderRepository;
import com.citrus.share.constants.QueueConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 審核訂單 Consumer
 * 接收來自 loancron 的觸發訊號，自己查詢並處理 PENDING 訂單
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewOrderConsumer {

    private final LoanOrderRepository loanOrderRepository;
    // private final ReviewOrderUsecase reviewOrderUsecase; // TODO: 之後實作

    @RabbitListener(queues = QueueConstants.LOANCORE_REVIEW_ORDER)
    public void handleReviewTrigger(String message) {
        log.info("=== Received Review Trigger ===");
        log.info("Trigger message: {}", message);

        // 自己查詢 PENDING 訂單
        List<LoanOrder> pendingOrders = loanOrderRepository.findByLoanState(LoanStateEnum.PENDING);

        if (pendingOrders.isEmpty()) {
            log.info("No pending orders found");
            return;
        }

        log.info("Found {} pending orders to review", pendingOrders.size());

        for (LoanOrder order : pendingOrders) {
            log.info("Processing order: {}", order.getLoanOrderId());

            // TODO: 觸發審核流程
            // reviewOrderUsecase.review(order.getLoanOrderId());
        }

        log.info("=== End of Review Processing ===");
    }
}

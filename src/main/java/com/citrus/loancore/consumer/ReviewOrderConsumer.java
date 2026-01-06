package com.citrus.loancore.consumer;

import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.citrus.loancore.enums.LoanStateEnum;
import com.citrus.loancore.model.LoanOrder;
import com.citrus.loancore.repository.LoanOrderRepository;
import com.citrus.loancore.usecase.query.LoanOrderQueryUsecase;
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

    private final LoanOrderQueryUsecase loanOrderQueryUsecase;

    @RabbitListener(queues = QueueConstants.LOANCORE_REVIEW_ORDER)
    public void handleReviewTrigger(String message) {
        log.info("=== Received Review Trigger ===");
        log.info("Trigger message: {}", message);

        loanOrderQueryUsecase.handlePendingOrders();

        log.info("=== End of Review Processing ===");
    }
}

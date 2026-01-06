package com.citrus.loancore.usecase.query;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.citrus.loancore.enums.LoanStateEnum;
import com.citrus.loancore.event.LoancoreEvent;
import com.citrus.loancore.model.LoanOrder;
import com.citrus.loancore.object.req.LoanOrderGetAllReq;
import com.citrus.loancore.object.req.LoanOrderGetByIdReq;
import com.citrus.loancore.service.query.LoanOrderQueryService;
import com.citrus.loancore.service.store.LoanOrderStoreService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanOrderQueryUsecase {

    private final LoanOrderQueryService loanOrderQueryService;
    private final LoanOrderStoreService loanOrderStoreService;
    private final LoancoreEvent loancoreEvent;

    public List<LoanOrder> findByUserId(LoanOrderGetAllReq req) {
        return loanOrderQueryService.findByUserId(req.getUserId());
    }

    public LoanOrder findById(LoanOrderGetByIdReq req) {
        return loanOrderQueryService.findById(req.getLoanOrderId());
    }

    @Transactional
    public void handlePendingOrders() {
        // 1. 認領 PENDING 訂單（FOR UPDATE SKIP LOCKED）
        List<LoanOrder> claimedOrders = loanOrderQueryService.claimPendingOrders(100);

        if (claimedOrders.isEmpty()) {
            log.info("No pending orders to process");
            return;
        }

        log.info("Claimed {} pending orders", claimedOrders.size());

        // 2. 更新訂單狀態為 BUREAU_CHECK（還在同一個 Transaction）
        for (LoanOrder order : claimedOrders) {
            loanOrderStoreService.updateState(order, LoanStateEnum.BUREAU_CHECK);
            log.info("Updated order {} to BUREAU_CHECK", order.getLoanOrderId());
        }

        // 3. 傳送訂單到 Bureau（TODO: 之後改用 Outbox）
        loancoreEvent.sendPendingOrderEvent(claimedOrders);
    }
}

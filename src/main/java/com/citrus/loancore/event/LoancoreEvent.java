package com.citrus.loancore.event;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.citrus.loancore.model.LoanOrder;
import com.citrus.loancore.model.LoancoreOutbox;
import com.citrus.loancore.object.dto.PendingOrderPayloadDto;
import com.citrus.loancore.service.store.LoancoreOutboxStoreService;
import com.citrus.share.enums.RabbitMQEnum;
import com.google.gson.Gson;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Loancore 事件發送器
 * 使用 Outbox Pattern 確保訊息可靠發送
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LoancoreEvent {

    private final LoancoreOutboxStoreService outboxStoreService;
    private final Gson gson;

    /**
     * 發送待審核訂單給 Bureau 處理
     * 每筆訂單寫入 Outbox，使用 saveAll 批次儲存
     */
    public void sendPendingOrderEvent(List<LoanOrder> pendingOrders) {
        if (pendingOrders == null || pendingOrders.isEmpty()) {
            log.info("No pending orders to send");
            return;
        }

        log.info("Saving {} pending orders to outbox", pendingOrders.size());

        // 建立 Outbox 訊息列表
        List<LoancoreOutbox> outboxList = new ArrayList<>();
        for (LoanOrder order : pendingOrders) {
            PendingOrderPayloadDto payload = PendingOrderPayloadDto.builder()
                    .loanOrderId(order.getLoanOrderId())
                    .userId(order.getUserId())
                    .panNumber(order.getPanNumber())
                    .name(order.getName())
                    .appliedAmount(order.getAppliedAmount() != null ? order.getAppliedAmount().toString() : null)
                    .build();

            LoancoreOutbox outbox = outboxStoreService.createOutbox(
                    RabbitMQEnum.PENDING_ORDER.getAggregateType(),
                    order.getLoanOrderId(),
                    RabbitMQEnum.PENDING_ORDER.name(),
                    gson.toJson(payload));

            outboxList.add(outbox);
        }

        // 批次儲存（一次 INSERT 多筆）
        outboxStoreService.saveAll(outboxList);

        log.info("Finished saving {} pending orders to outbox", outboxList.size());
    }
}

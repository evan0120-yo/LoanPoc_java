package com.citrus.share.enums;

import java.util.List;

import com.citrus.share.constants.QueueConstants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RabbitMQEnum {

        // 訂單創建事件
        ORDER_CREATED(
                        "ORIGIN",
                        "loan.order.exchange",
                        ExchangeTypeEnum.TOPIC,
                        List.of(
                                        QueueConstants.LOANCORE_ORDER_CREATED// 之後可以加更多訂閱者的 Queue
                        ),
                        "order.created"),

        REVIEW_ORDER(
                        "LOANCRON",
                        "loan.review.exchange",
                        ExchangeTypeEnum.TOPIC,
                        List.of(
                                        QueueConstants.LOANCORE_REVIEW_ORDER),
                        "order.review"),

        PENDING_ORDER(
                        "LOANCORE",
                        "loan.pending.exchange",
                        ExchangeTypeEnum.TOPIC,
                        List.of(
                                        QueueConstants.BUREAU_PENDING_ORDER),
                        "order.pending"),

        // // 訂單狀態更新
        // ORDER_UPDATED(
        // "LOAN_ORDER",
        // "loan.order.exchange",
        // ExchangeTypeEnum.TOPIC,
        // List.of(
        // QueueConstants.ORIGIN_ORDER_UPDATED,
        // QueueConstants.SIGN_ORDER_UPDATED
        // ),
        // "order.updated"
        // ),

        // // 簽約完成
        // SIGN_COMPLETED(
        // "SIGN",
        // "loan.sign.exchange",
        // ExchangeTypeEnum.TOPIC,
        // List.of(
        // QueueConstants.LOANCORE_SIGN_COMPLETED
        // ),
        // "sign.completed"
        // ),

        ;

        private final String aggregateType;
        private final String exchangeName;
        private final ExchangeTypeEnum exchangeType;
        private final List<String> queueList; // 訂閱此事件的 Queue 列表
        private final String routingKey;
}

package com.citrus.share.enums;

import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RabbitMQEnum {

    // 訂單創建事件
    ORDER_CREATED(
            "loan.order.exchange", // exchange
            ExchangeTypeEnum.TOPIC, // exchange type
            Map.of(
                    "LOANCORE", "loancore.order.created.queue"// 訂閱者 → 隊列名稱
            ),
            "order.created"// routing key
    ),

    // // 訂單狀態更新
    // ORDER_UPDATED(
    // "loan.order.exchange",
    // ExchangeTypes.TOPIC,
    // Map.of(
    // "ORIGIN", "origin.order.updated.queue",
    // "SIGN", "sign.order.updated.queue"),
    // "order.updated"4),

    // // 簽約完成
    // SIGN_COMPLETED(
    // "loan.sign.exchange",
    // ExchangeTypes.TOPIC,
    // Map.of(
    // "LOANCORE", "loancore.sign.completed.queue"),
    // "sign.completed"),

    ;

    private final String exchangeName;
    private final ExchangeTypeEnum exchangeType;
    private final Map<String, String> queueMap; // 訂閱者 → 隊列名稱
    private final String routingKey;
}

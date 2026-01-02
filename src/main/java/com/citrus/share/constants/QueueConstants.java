package com.citrus.share.constants;

/**
 * RabbitMQ Queue 常量
 * 讓 RabbitMQEnum 和 @RabbitListener 都用同一個來源
 */
public class QueueConstants {

    // ========== LOANCORE ==========
    public static final String LOANCORE_ORDER_CREATED = "loancore.order.created.queue";

    // ========== ORIGIN ==========
    // public static final String ORIGIN_ORDER_UPDATED =
    // "origin.order.updated.queue";

    // ========== SIGN ==========
    // public static final String SIGN_ORDER_UPDATED = "sign.order.updated.queue";
}

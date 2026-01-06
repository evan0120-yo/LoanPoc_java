package com.citrus.share.constants;

/**
 * RabbitMQ Queue 常量
 * 讓 RabbitMQEnum 和 @RabbitListener 都用同一個來源
 */
public class QueueConstants {

    // ========== LOANCORE ==========
    public static final String LOANCORE_ORDER_CREATED = "loancore.order.created.queue";

    // ========== LOANCRON ==========
    public static final String LOANCORE_REVIEW_ORDER = "loancore.order.review.queue";
    public static final String BUREAU_PENDING_ORDER = "bureau.order.pending.queue";
}

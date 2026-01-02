package com.citrus.common.service;

import com.citrus.common.model.OutboxMessage;

/**
 * Outbox Service 介面
 * 各模組實作此介面提供 Outbox 操作邏輯
 * 
 * @param <T> 繼承 OutboxMessage 的具體類型
 */
public interface OutboxService<T extends OutboxMessage> {

    /**
     * 儲存 Outbox 訊息
     * 
     * @param aggregateType 聚合根類型 (例如：LOAN_ORDER)
     * @param aggregateId   聚合根 ID (例如：orderId)
     * @param eventType     事件類型 (例如：ORDER_CREATED)
     * @param payload       事件資料 (會被序列化成 JSON)
     * @return 儲存的 Outbox 訊息
     */
    T save(String aggregateType, String aggregateId, String eventType, Object payload);

    /**
     * 標記為處理中（發送前）
     * 
     * @param outboxId Outbox 訊息 ID
     */
    void markAsProcessing(String outboxId);

    /**
     * 標記為已發送
     * 
     * @param outboxId Outbox 訊息 ID
     */
    void markAsSent(String outboxId);

    /**
     * 標記為失敗
     * 
     * @param outboxId     Outbox 訊息 ID
     * @param errorMessage 錯誤訊息
     */
    void markAsFailed(String outboxId, String errorMessage);

    /**
     * 增加重試次數
     * 
     * @param outboxId Outbox 訊息 ID
     */
    void incrementRetryCount(String outboxId);
}

package com.citrus.common.service;

import com.citrus.common.model.OutboxMessage;

/**
 * Outbox Service 介面
 * 提供業務邏輯層寫入 Outbox 的方法
 * 
 * 注意：Worker 直接透過 Repository 處理狀態更新，
 * 此介面僅提供業務邏輯層寫入 Outbox 的功能
 * 
 * @param <T> 繼承 OutboxMessage 的具體類型
 */
public interface OutboxService<T extends OutboxMessage> {

    /**
     * 儲存 Outbox 訊息（與業務邏輯在同一個事務中）
     * 
     * @param aggregateType 聚合根類型 (例如：LOAN_ORDER)
     * @param aggregateId   聚合根 ID (例如：orderId)
     * @param eventType     事件類型 (例如：ORDER_CREATED)
     * @param payload       事件資料 (會被序列化成 JSON)
     * @return 儲存的 Outbox 訊息
     */
    T save(String aggregateType, String aggregateId, String eventType, Object payload);
}

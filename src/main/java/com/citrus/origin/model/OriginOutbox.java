package com.citrus.origin.model;

import com.citrus.common.model.OutboxMessage;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

/**
 * Origin 模組的 Outbox 訊息
 * 繼承 common.OutboxMessage，自動擁有所有欄位
 */
@Entity
@Table(name = "origin_outbox")
@NoArgsConstructor // JPA Entity 必須有無參構造函數
public class OriginOutbox extends OutboxMessage {
    // 如果未來有 origin 特有欄位，可以加在這裡
}

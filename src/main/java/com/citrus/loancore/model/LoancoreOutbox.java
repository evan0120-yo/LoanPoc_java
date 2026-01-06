package com.citrus.loancore.model;

import com.citrus.common.model.OutboxMessage;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

/**
 * Loancore Outbox 訊息實體
 * 繼承 common.OutboxMessage，自動擁有所有欄位
 */
@Entity
@Table(name = "loancore_outbox")
@NoArgsConstructor
public class LoancoreOutbox extends OutboxMessage {
    // 繼承 OutboxMessage 所有欄位
    // 如需擴展欄位可在此添加
}

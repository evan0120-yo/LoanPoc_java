package com.citrus.common.enums;

import lombok.Getter;

@Getter
public enum OutboxStatusEnum {
    PENDING, // 待發送
    PROCESSING, // 發送中（鎖定狀態，避免重複處理）
    SENT, // 已發送
    FAILED // 失敗（超過重試次數）
}

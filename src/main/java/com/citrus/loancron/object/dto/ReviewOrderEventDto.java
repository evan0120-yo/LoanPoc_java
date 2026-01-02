package com.citrus.loancron.object.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 審核訂單事件 DTO
 * 發送給 loancore 處理訂單審核
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewOrderEventDto {
    private String orderId;
    private Instant triggerAt;
}

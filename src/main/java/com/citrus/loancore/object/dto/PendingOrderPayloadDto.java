package com.citrus.loancore.object.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 待審核訂單 Payload
 * 用於 MQ 傳送給 Bureau 處理
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingOrderPayloadDto {

    private String loanOrderId;
    private String userId;
    private String panNumber;
    private String name;
    private String appliedAmount;
}

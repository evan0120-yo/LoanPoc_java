package com.citrus.bureau.object.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * CIBIL 查詢結果 DTO (甲方通訊用)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CibilResultDto {

    private Integer score;
    private Integer activeAccounts;
    private Integer overdueAccounts;
    private BigDecimal totalOutstanding;
    private List<TradelineDto> tradelines;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradelineDto {
        private String accountType;
        private String lenderName;
        private BigDecimal sanctionedAmount;
        private BigDecimal currentBalance;
        private Integer dpd;
        private String status;
    }
}

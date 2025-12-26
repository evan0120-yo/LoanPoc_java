package com.citrus.bureau.object.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PAN 驗證 API 請求 DTO (甲方通訊用)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PanVerifyDto {

    private String panNumber;
    private String name;
    private String dateOfBirth;
}

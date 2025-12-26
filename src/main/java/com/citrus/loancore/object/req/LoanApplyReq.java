package com.citrus.loancore.object.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 貸款申請請求
 */
@Data
public class LoanApplyReq {

    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "mobile is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
    private String mobile;

    @NotBlank(message = "panNumber is required")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]$", message = "Invalid PAN format")
    private String panNumber;

    private String name;

    @Positive(message = "appliedAmount must be positive")
    private BigDecimal appliedAmount;
}

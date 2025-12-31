package com.citrus.origin.object.req;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplyReq {

    // ========== 用戶基本資訊 ==========
    private String userId; // 用戶 ID
    private String mobile; // 手機號碼（10 位數字，例如：9876543210）
    private String panNumber; // PAN 號碼（10 位英數字，例如：ABCDE1234F）
    private String name; // 用戶姓名

    // ========== 申請資訊 ==========
    private BigDecimal appliedAmount; // 申請金額（₹，例如：50000.00）

    // ========== 銀行資訊 ==========
    private String bankAccount; // 銀行帳號（例如：12345678901234）
    private String ifscCode; // IFSC 代碼（11 位，例如：SBIN0001234）
    private String bankName; // 銀行名稱（例如：State Bank of India）
}

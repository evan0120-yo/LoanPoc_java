package com.citrus.loancore.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LoanStateEnum {
    // ===== 申請階段 =====
    PENDING, // 待處理
    BUREAU_CHECK, // 徵信查詢中

    // ===== 審核階段 =====
    UNDERWRITING, // 審核中
    REJECTED, // 已拒絕 (終態)
    LSP_ROUTING, // 轉導合作商 (終態)

    // ===== 簽約階段 =====
    OFFER_READY, // Offer 已產生
    SIGN_PENDING, // 待簽約
    SIGNED, // 已簽約

    // ===== 放款階段 =====
    DISBURSAL_PENDING, // 待放款
    DISBURSAL_FAILED, // 放款失敗
    DISBURSED, // 已放款

    // ===== 還款階段 =====
    ACTIVE, // 還款中
    OVERDUE, // 逾期
    NPA, // 不良資產 (90+ DPD)

    // ===== 結案 =====
    CLOSED, // 已結清 (終態)
    WRITTEN_OFF, // 已呆帳核銷 (終態)
    CANCELLED // 已取消 (終態)

}

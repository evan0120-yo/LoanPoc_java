package com.citrus.origin.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RejectReasonEnum {
    BLACKLIST, // 在黑名單中
    CIBIL_LOW, // CIBIL 信用分數太低
    FOIR_HIGH, // 負債收入比太高
    INCOME_LOW, // 收入太低
    EXISTING_LOAN, // 已有未結清貸款
    AGE_INVALID, // 年齡不符
    DEDUPE_FAIL, // 重複申請 (短期內申請多次)
}

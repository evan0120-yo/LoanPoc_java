package com.citrus.share.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 統一錯誤碼
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 通用錯誤 1xxxx
    SUCCESS(0, "Success"),
    SYSTEM_ERROR(10001, "System error"),
    INVALID_PARAM(10002, "Invalid parameter"),
    DATA_NOT_FOUND(10003, "Data not found"),
    DUPLICATE_REQUEST(10004, "Duplicate request"),

    // Bureau 模組 2xxxx
    BUREAU_PAN_INVALID(20001, "Invalid PAN number"),
    BUREAU_CIBIL_FAILED(20002, "CIBIL query failed"),
    BUREAU_BSA_PARSE_ERROR(20003, "BSA parse error"),

    // Origin 模組 3xxxx
    ORIGIN_DEDUPE_FAILED(30001, "Dedupe check failed"),
    ORIGIN_BLACKLIST_HIT(30002, "Blacklisted user"),
    ORIGIN_REJECTED(30003, "Application rejected"),

    // Sign 模組 4xxxx
    SIGN_KFS_GENERATE_FAILED(40001, "KFS generation failed"),
    SIGN_PENNY_DROP_FAILED(40002, "Penny drop verification failed"),
    SIGN_ESIGN_FAILED(40003, "E-Sign failed"),
    SIGN_ENACH_FAILED(40004, "E-NACH setup failed"),

    // Pay 模組 5xxxx
    PAY_DISBURSAL_FAILED(50001, "Disbursal failed"),
    PAY_COLLECTION_FAILED(50002, "Collection failed"),
    PAY_WEBHOOK_INVALID(50003, "Invalid webhook"),

    // Knockoff 模組 6xxxx
    KNOCKOFF_INSUFFICIENT_AMOUNT(60001, "Insufficient repayment amount"),

    // Ledger 模組 7xxxx
    LEDGER_IMBALANCE(70001, "Ledger imbalance detected"),

    // Callect 模組 8xxxx
    CALLECT_ASSIGN_FAILED(80001, "Case assignment failed"),

    // LoanCore 模組 9xxxx
    LOAN_STATE_INVALID(90001, "Invalid loan state transition"),
    LOAN_NOT_FOUND(90002, "Loan not found");

    private final int code;
    private final String message;
}

package com.citrus.share.constant;

/**
 * 應用常數
 */
public final class AppConstant {

    private AppConstant() {
    }

    // ========== 快取相關 ==========
    /** Bureau 查詢快取天數 */
    public static final int BUREAU_CACHE_DAYS = 30;

    // ========== 金額相關 ==========
    /** Penny Drop 金額 (Rupee) */
    public static final int PENNY_DROP_AMOUNT = 1;

    // ========== DPD 分桶 ==========
    /** B0: 0 天 */
    public static final int DPD_B0 = 0;
    /** B1: 1-30 天 */
    public static final int DPD_B1_MAX = 30;
    /** B2: 31-60 天 */
    public static final int DPD_B2_MAX = 60;
    /** B3: 61-90 天 */
    public static final int DPD_B3_MAX = 90;
    /** NPA: 90+ 天 */
    public static final int DPD_NPA_THRESHOLD = 90;

    // ========== MQ 相關 ==========
    public static final String MQ_EXCHANGE_LOAN = "loan.exchange";
    public static final String MQ_QUEUE_LEDGER = "ledger.queue";
    public static final String MQ_QUEUE_PAYMENT = "payment.queue";
    public static final String MQ_ROUTING_LEDGER = "ledger.routing";
    public static final String MQ_ROUTING_PAYMENT = "payment.routing";
}

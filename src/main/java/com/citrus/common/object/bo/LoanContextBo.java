package com.citrus.common.object.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 貸款流程上下文 BO
 * 「資料夾模式」：使用者帶著這個 BO 跑各個關卡
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanContextBo {

    // ========== 基本資訊 ==========
    /** 貸款記錄 ID */
    private Long loanRecordId;

    /** 使用者 ID */
    private String userId;

    /** 手機號碼 */
    private String mobile;

    /** PAN 號碼 (稅號) */
    private String panNumber;

    // ========== Bureau 資訊 ==========
    /** CIBIL 分數 */
    private Integer cibilScore;

    /** FOIR (償債能力比) */
    private BigDecimal foir;

    /** Bureau 查詢是否完成 */
    private Boolean bureauCompleted;

    // ========== Origin 資訊 ==========
    /** 是否通過 Dedupe */
    private Boolean dedupePass;

    /** 是否命中黑名單 */
    private Boolean blacklistHit;

    /** 核准額度 */
    private BigDecimal approvedLimit;

    /** 年利率 ROI */
    private BigDecimal roi;

    /** 貸款年期 (月) */
    private Integer tenure;

    // ========== Sign 資訊 ==========
    /** KFS 文件 URL */
    private String kfsUrl;

    /** Penny Drop 驗證結果 */
    private Boolean pennyDropVerified;

    /** e-Sign 完成 */
    private Boolean eSignCompleted;

    /** e-NACH 設定完成 */
    private Boolean eNachCompleted;

    // ========== Pay 資訊 ==========
    /** 放款 UTR */
    private String disbursalUtr;

    /** 放款日期 */
    private LocalDate disbursalDate;

    // ========== 狀態追蹤 ==========
    /** 當前狀態 */
    private String currentState;

    /** 上一個狀態 */
    private String previousState;

    /** 錯誤訊息 (如有) */
    private String errorMessage;
}

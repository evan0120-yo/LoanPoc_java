package com.citrus.sign.model;

import com.citrus.common.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 簽約單
 */
@Entity
@Table(name = "sign_orders", indexes = {
        @Index(name = "idx_sign_loan", columnList = "loanRecordId")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignOrder extends BaseModel {

    @Column(name = "loan_record_id", nullable = false)
    private Long loanRecordId;

    // ===== KFS 生成 =====
    @Enumerated(EnumType.STRING)
    @Column(name = "kfs_status", length = 20)
    @Builder.Default
    private StepStatus kfsStatus = StepStatus.PENDING;

    @Column(name = "kfs_url", length = 500)
    private String kfsUrl;

    // ===== Penny Drop =====
    @Enumerated(EnumType.STRING)
    @Column(name = "penny_drop_status", length = 20)
    @Builder.Default
    private StepStatus pennyDropStatus = StepStatus.PENDING;

    @Column(name = "penny_drop_utr", length = 50)
    private String pennyDropUtr;

    // ===== e-Sign =====
    @Enumerated(EnumType.STRING)
    @Column(name = "esign_status", length = 20)
    @Builder.Default
    private StepStatus esignStatus = StepStatus.PENDING;

    @Column(name = "esign_ref", length = 100)
    private String esignRef;

    // ===== e-NACH =====
    @Enumerated(EnumType.STRING)
    @Column(name = "enach_status", length = 20)
    @Builder.Default
    private StepStatus enachStatus = StepStatus.PENDING;

    @Column(name = "enach_ref", length = 100)
    private String enachRef;

    @Column(name = "umrn", length = 50)
    private String umrn;

    // ===== 整體狀態 =====
    @Enumerated(EnumType.STRING)
    @Column(name = "overall_status", nullable = false, length = 20)
    @Builder.Default
    private OverallStatus overallStatus = OverallStatus.PENDING;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    public enum StepStatus {
        PENDING, // 待處理
        IN_PROGRESS, // 進行中
        SUCCESS, // 成功
        FAILED // 失敗
    }

    public enum OverallStatus {
        PENDING, // 待開始
        IN_PROGRESS, // 簽約中
        COMPLETED, // 簽約完成
        FAILED, // 簽約失敗
        EXPIRED // 已過期
    }

    /**
     * 檢查是否全部完成
     */
    public boolean isAllStepsCompleted() {
        return kfsStatus == StepStatus.SUCCESS &&
                pennyDropStatus == StepStatus.SUCCESS &&
                esignStatus == StepStatus.SUCCESS &&
                enachStatus == StepStatus.SUCCESS;
    }
}

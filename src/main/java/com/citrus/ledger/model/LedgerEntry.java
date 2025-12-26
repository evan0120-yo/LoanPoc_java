package com.citrus.ledger.model;

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

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 會計分錄 (Journal Entry)
 * 複式簿記：不可修改，只能紅沖藍補
 */
@Entity
@Table(name = "ledger_entries", indexes = {
        @Index(name = "idx_ledger_loan", columnList = "loanRecordId"),
        @Index(name = "idx_ledger_type", columnList = "entryType"),
        @Index(name = "idx_ledger_date", columnList = "postingDate")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntry extends BaseModel {

    @Column(name = "loan_record_id", nullable = false)
    private Long loanRecordId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 30)
    private EntryType entryType;

    @Column(name = "posting_date", nullable = false)
    private LocalDate postingDate;

    @Column(name = "description", length = 200)
    private String description;

    // ===== 借方 =====
    @Column(name = "debit_account", nullable = false, length = 50)
    private String debitAccount;

    @Column(name = "debit_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal debitAmount;

    // ===== 貸方 =====
    @Column(name = "credit_account", nullable = false, length = 50)
    private String creditAccount;

    @Column(name = "credit_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal creditAmount;

    // ===== 參考 =====
    @Column(name = "reference_type", length = 30)
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    /** 是否為沖銷分錄 */
    @Column(name = "is_reversal")
    @Builder.Default
    private Boolean isReversal = false;

    /** 被沖銷的分錄 ID */
    @Column(name = "reversed_entry_id")
    private Long reversedEntryId;

    public enum EntryType {
        DISBURSAL, // 放款
        PRINCIPAL, // 本金回收
        INTEREST, // 利息收入
        PENALTY, // 滯納金
        GST, // 稅金
        ACCRUAL, // 計息
        WRITE_OFF, // 呆帳核銷
        REVERSAL // 沖銷
    }

    /**
     * 驗證借貸平衡
     */
    public boolean isBalanced() {
        return debitAmount.compareTo(creditAmount) == 0;
    }
}

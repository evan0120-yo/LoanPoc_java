package com.citrus.bureau.factory.bsa.object.bo;

import java.math.BigDecimal;

import com.citrus.bureau.factory.HttpLogBo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BsaResultBo extends HttpLogBo {

    /** 分析是否成功 */
    private Boolean isSuccess;

    /** 月均收入（用於 FOIR 計算） */
    private BigDecimal monthlyIncome;

    /** 月均餘額 */
    private BigDecimal avgMonthlyBalance;

    /** 最低餘額（財務健康度指標） */
    private BigDecimal minBalance;

    /** 薪資規律性：REGULAR / IRREGULAR / UNKNOWN */
    private String salaryRegularity;

    /** 薪資入帳日（例如每月 1 號） */
    private Integer salaryDay;

    /** 分析了幾個月的資料 */
    private Integer monthsAnalyzed;

    /** 退票次數（重要風險指標） */
    private Integer bounceCheckCount;

    /** 現有 EMI 支出次數 */
    private Integer emiOutflowCount;

    /** 現有 EMI 月支出總額 */
    private BigDecimal emiOutflowAmount;

    /** 失敗時的錯誤碼 */
    private String errorCode;

    /** 失敗時的錯誤訊息 */
    private String errorMessage;
}

package com.citrus.bureau.factory.cibil.object.bo;

import java.math.BigDecimal;
import java.time.LocalDate;

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
public class CibilResultBo extends HttpLogBo {

    /** 查詢是否成功 */
    private Boolean isSuccess;

    /** 信用分數（300-900） */
    private Integer cibilScore;

    /** 分數等級描述：POOR / FAIR / GOOD / EXCELLENT */
    private String scoreDescription;

    /** 現有貸款數量 */
    private Integer existingLoanCount;

    /** 活躍帳戶數 */
    private Integer activeAccounts;

    /** 總負債金額（₹） */
    private BigDecimal totalExposure;

    /** 逾期金額 */
    private BigDecimal overdueAmount;

    /** 是否有違約紀錄 */
    private Boolean hasDefaultHistory;

    /** 最近一次被查詢日期 */
    private LocalDate lastEnquiryDate;

    /** 近 6 個月被查詢次數 */
    private Integer enquiryCountLast6Months;

    /** 失敗時的錯誤碼 */
    private String errorCode;

    /** 失敗時的錯誤訊息 */
    private String errorMessage;
}

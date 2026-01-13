package com.citrus.bureau.factory.pan.object.bo;

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
public class PanResultBo extends HttpLogBo {

    /** 查詢是否成功 */
    private Boolean isSuccess;

    /** PAN 號碼 */
    private String panNumber;

    /** PAN 上登記的姓名 */
    private String nameOnPan;

    /** 輸入的姓名是否與 PAN 匹配 */
    private Boolean nameMatch;

    /** PAN 狀態：VALID / INVALID / INACTIVE */
    private String panStatus;

    /** PAN 類型：PERSON / COMPANY / HUF 等 */
    private String panType;

    /** 失敗時的錯誤碼 */
    private String errorCode;

    /** 失敗時的錯誤訊息 */
    private String errorMessage;
}

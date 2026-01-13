package com.citrus.bureau.factory.cibil;

import com.citrus.bureau.factory.cibil.object.bo.CibilResultBo;
import com.citrus.bureau.factory.cibil.object.bo.CibilVerifyBo;

public interface CibilFactory {

    /**
     * 查詢 CIBIL 信用分數
     * 
     * @param input 查詢請求（panNumber）
     * @return 查詢結果
     */
    CibilResultBo query(CibilVerifyBo input);
}

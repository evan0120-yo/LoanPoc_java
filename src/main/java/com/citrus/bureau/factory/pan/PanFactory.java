package com.citrus.bureau.factory.pan;

import com.citrus.bureau.factory.pan.object.bo.PanResultBo;
import com.citrus.bureau.factory.pan.object.bo.PanVerifyBo;

public interface PanFactory {

    /**
     * 驗證 PAN 號碼
     * 
     * @param input 驗證請求（panNumber, name）
     * @return 驗證結果
     */
    PanResultBo verify(PanVerifyBo input);
}

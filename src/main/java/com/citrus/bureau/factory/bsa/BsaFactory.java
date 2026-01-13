package com.citrus.bureau.factory.bsa;

import com.citrus.bureau.factory.bsa.object.bo.BsaResultBo;
import com.citrus.bureau.factory.bsa.object.bo.BsaVerifyBo;

public interface BsaFactory {

    /**
     * 分析銀行流水
     * 
     * @param input 分析請求（bankAccountNumber）
     * @return 分析結果
     */
    BsaResultBo analyze(BsaVerifyBo input);
}

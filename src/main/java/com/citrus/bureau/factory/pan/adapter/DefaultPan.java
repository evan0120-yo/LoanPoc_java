package com.citrus.bureau.factory.pan.adapter;

import org.springframework.stereotype.Service;

import com.citrus.bureau.factory.pan.PanFactory;
import com.citrus.bureau.factory.pan.object.bo.PanResultBo;
import com.citrus.bureau.factory.pan.object.bo.PanVerifyBo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultPan implements PanFactory {

    @Override
    public PanResultBo verify(PanVerifyBo input) {
        log.info("DefaultPan.verify() - panNumber: {}, name: {}", input.getPanNumber(), input.getName());

        // POC: 回傳假資料，全部成功
        return PanResultBo.builder()
                .isSuccess(true)
                .panNumber(input.getPanNumber())
                .nameOnPan(input.getName())
                .nameMatch(true)
                .panStatus("VALID")
                .panType("PERSON")
                .build();
    }
}

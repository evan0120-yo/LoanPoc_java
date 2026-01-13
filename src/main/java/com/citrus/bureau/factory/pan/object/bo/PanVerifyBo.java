package com.citrus.bureau.factory.pan.object.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PanVerifyBo {
    private String panNumber;
    private String name;
}

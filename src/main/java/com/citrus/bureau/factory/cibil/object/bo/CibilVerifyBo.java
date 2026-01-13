package com.citrus.bureau.factory.cibil.object.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CibilVerifyBo {
    private String panNumber;
}

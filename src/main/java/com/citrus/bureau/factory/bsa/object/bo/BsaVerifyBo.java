package com.citrus.bureau.factory.bsa.object.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BsaVerifyBo {
    private String bankAccountNumber;
}

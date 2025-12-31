package com.citrus.origin.object.req;

import com.citrus.origin.enums.IdentifierEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistSaveReq {
    private String userId;
    private String addedBy;
    private IdentifierEnum identifier;
    private String identifierValue;
    private String reason;
}

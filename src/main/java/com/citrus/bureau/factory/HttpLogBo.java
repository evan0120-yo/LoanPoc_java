package com.citrus.bureau.factory;

import java.time.Instant;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class HttpLogBo {
    private String reqBody;
    private Map<String, String> reqHeader;
    private String respBody;
    private Map<String, String> respHeader;
    private Integer statusCode;
    private Instant respAt;
}

package com.citrus.share.enums;

import lombok.Getter;

@Getter
public enum ExchangeTypeEnum {
    DIRECT,
    TOPIC,
    FANOUT,
    HEADERS
}

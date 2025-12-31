package com.citrus.origin.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DecisionResultEnum {
    APPROVED,
    REJECTED,
    LSP_ROUTING;
}

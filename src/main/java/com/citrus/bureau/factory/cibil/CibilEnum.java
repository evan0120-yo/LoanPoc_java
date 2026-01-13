package com.citrus.bureau.factory.cibil;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CibilEnum {

    DEFAULT("defaultCibil")

    ;

    private final String className;
}

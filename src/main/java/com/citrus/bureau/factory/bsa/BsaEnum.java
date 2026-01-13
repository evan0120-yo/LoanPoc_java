package com.citrus.bureau.factory.bsa;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BsaEnum {

    DEFAULT("defaultBsa")

    ;

    private final String className;
}

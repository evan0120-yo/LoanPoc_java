package com.citrus.bureau.factory.pan;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PanEnum {

    DEFAULT("defaultPan")

    ;

    private final String className;
}

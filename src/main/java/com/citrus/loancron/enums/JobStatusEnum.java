package com.citrus.loancron.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JobStatusEnum {
    PENDING,
    RUNNING,
    SUCCESS,
    FAILED;
}

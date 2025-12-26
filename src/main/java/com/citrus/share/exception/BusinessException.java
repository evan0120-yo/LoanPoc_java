package com.citrus.share.exception;

import lombok.Getter;

/**
 * 業務例外
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object data;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.data = null;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.data = null;
    }

    public BusinessException(ErrorCode errorCode, Object data) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.data = data;
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.data = null;
    }

    public int getCode() {
        return errorCode.getCode();
    }
}

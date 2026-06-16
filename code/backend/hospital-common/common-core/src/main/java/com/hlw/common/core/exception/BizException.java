package com.hlw.common.core.exception;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException {
    /**
     * 业务错误码
     */
    private final int code;

    public BizException(String message) {
        this(500, message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

}

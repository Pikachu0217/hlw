package com.hlw.common.core.exception;

public class BizException extends RuntimeException {
    private final int code;

    public BizException(String message) {
        this(500, message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 获取业务错误码。
     *
     * @return 业务错误码
     */
    public int getCode() {
        return code;
    }
}

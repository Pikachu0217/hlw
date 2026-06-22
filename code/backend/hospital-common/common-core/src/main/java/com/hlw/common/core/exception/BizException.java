package com.hlw.common.core.exception;

import com.hlw.common.core.enums.HttpStatusEnum;
import lombok.Getter;

/**
 * 业务异常。
 */
@Getter
public class BizException extends RuntimeException {
    /**
     * 业务错误码
     */
    private final int code;

    /**
     * 构造默认业务异常。
     *
     * @param message 异常消息
     */
    public BizException(String message) {
        this(500, message);
    }

    /**
     * 构造指定错误码的业务异常。
     *
     * @param code 业务错误码
     * @param message 异常消息
     */
    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 通过业务状态枚举构造异常。
     *
     * @param httpStatusEnum 业务状态枚举
     */
    public BizException(HttpStatusEnum httpStatusEnum) {
        super(httpStatusEnum.getMessage());
        this.code = httpStatusEnum.getCode();
    }

    /**
     * 通过业务状态枚举和占位符参数构造异常。
     *
     * @param httpStatusEnum 业务状态枚举
     * @param args 消息占位符参数
     */
    public BizException(HttpStatusEnum httpStatusEnum, Object... args) {
        super(httpStatusEnum.formatMessage(args));
        this.code = httpStatusEnum.getCode();
    }
}

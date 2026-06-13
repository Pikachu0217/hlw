package com.hlw.system.controller;

import com.hlw.common.core.domain.R;
import com.hlw.common.core.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 系统服务异常处理器，统一返回业务异常和未知异常。
 */
@RestControllerAdvice
public class SystemExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(SystemExceptionHandler.class);

    /**
     * 处理业务异常。
     *
     * @param exception 业务异常
     * @return 业务错误响应
     */
    @ExceptionHandler(BizException.class)
    public R<Void> handleBizException(BizException exception) {
        log.warn("系统管理业务异常，code={}，message={}", exception.getCode(), exception.getMessage());
        return R.fail(exception.getCode(), exception.getMessage());
    }

    /**
     * 处理未知异常。
     *
     * @param exception 未知异常
     * @return 系统错误响应
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception exception) {
        log.error("系统管理未知异常", exception);
        return R.fail(500, "系统服务暂不可用");
    }
}

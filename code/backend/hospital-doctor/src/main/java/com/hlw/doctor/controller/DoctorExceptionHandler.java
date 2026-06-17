package com.hlw.doctor.controller;

import com.hlw.common.core.domain.R;
import com.hlw.common.core.exception.BizException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

/**
 * 医生服务异常处理器，统一返回业务异常和未知异常。
 */
@RestControllerAdvice
@Slf4j
public class DoctorExceptionHandler {
    /**
     * 处理业务异常。
     *
     * @param exception 业务异常
     * @return 业务错误响应
     */
    @ExceptionHandler(BizException.class)
    public R<Void> handleBizException(BizException exception) {
        log.warn("医生服务业务异常，code={}，message={}", exception.getCode(), exception.getMessage());
        return R.fail(exception.getCode(), exception.getMessage());
    }

    /**
     * 处理请求参数校验异常。
     *
     * @param exception 参数校验异常
     * @return 参数错误响应
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, MethodArgumentTypeMismatchException.class})
    public R<Void> handleValidationException(Exception exception) {
        String message = "请求参数不合法";
        if (exception instanceof MethodArgumentNotValidException methodArgumentNotValidException
            && methodArgumentNotValidException.getBindingResult().getFieldError() != null) {
            message = methodArgumentNotValidException.getBindingResult().getFieldError().getDefaultMessage();
        }
        if (exception instanceof BindException bindException
            && bindException.getBindingResult().getFieldError() != null) {
            message = bindException.getBindingResult().getFieldError().getDefaultMessage();
        }
        if (exception instanceof MethodArgumentTypeMismatchException methodArgumentTypeMismatchException) {
            message = methodArgumentTypeMismatchException.getName() + "参数格式不正确";
        }
        log.warn("医生服务参数校验异常，message={}", message);
        return R.fail(400, message);
    }

    /**
     * 处理未知异常。
     *
     * @param exception 未知异常
     * @return 系统错误响应
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception exception) {
        log.error("医生服务未知异常", exception);
        return R.fail(500, "医生服务暂不可用");
    }
}

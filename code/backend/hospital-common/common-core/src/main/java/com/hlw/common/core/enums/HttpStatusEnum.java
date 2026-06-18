package com.hlw.common.core.enums;

public enum HttpStatusEnum {

    // AUTH/SYSTEM 模块开始
    LOGIN_PARAMETERS_CANNOT_BE_NULL(10001, "登录参数不能为空"),
    USERNAME_OR_PASSWORD_ERROR(10002, "用户名或密码错误"),
    LOGIN_USER_DOES_NOT_EXIST(10003, "登录用户不存在"),
    LOGOUT_TOKEN_REQUIRED(10004, "退出登录失败，未携带有效令牌"),
    INVALID_TENANT_ID(10005, "租户编号无效"),


    // AUTH/SYSTEM 模块结束

    ;

    private final int code;
    private final String message;

    HttpStatusEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}

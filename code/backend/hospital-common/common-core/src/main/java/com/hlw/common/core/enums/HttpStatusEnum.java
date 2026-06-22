package com.hlw.common.core.enums;

import java.util.regex.Matcher;

/**
 * 统一业务状态枚举。
 */
public enum HttpStatusEnum {

    // AUTH/SYSTEM/Gateway 模块开始
    LOGIN_PARAMETERS_CANNOT_BE_NULL(10001, "登录参数不能为空"),
    USERNAME_OR_PASSWORD_ERROR(10002, "用户名或密码错误"),
    LOGIN_USER_DOES_NOT_EXIST(10003, "登录用户不存在"),
    LOGOUT_TOKEN_REQUIRED(10004, "退出登录失败，未携带有效令牌"),
    INVALID_TENANT_ID(10005, "租户编号无效"),
    REMOTE_SYSTEM_RESPONSE_NULL(10006, "{}"),
    REMOTE_SYSTEM_RESPONSE_ERROR(10007, "{}"),
    GATEWAY_ROUTE_CONFIG_NOT_FOUND(10008, "网关路由配置不存在"),
    SYSTEM_ENTITY_NOT_FOUND(10009, "{}"),
    PLATFORM_TENANT_CONTEXT_FORBIDDEN(10010, "{}"),
    TENANT_CONTEXT_INVALID(10011, "租户上下文无效"),
    TENANT_PACKAGE_REQUIRED(10012, "租户套餐不能为空"),
    MENU_NOT_FOUND(10013, "菜单不存在"),
    SYSTEM_DEFAULT_DATA_OPERATION_FORBIDDEN(10014, "禁止{}系统默认{}"),

    // AUTH/SYSTEM/Gateway 模块结束

    ;

    private final int code;
    private final String message;

    /**
     * 构造业务状态枚举。
     *
     * @param code 业务状态码
     * @param message 业务提示消息模板
     */
    HttpStatusEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取业务状态码。
     *
     * @return 业务状态码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取业务提示消息模板。
     *
     * @return 业务提示消息模板
     */
    public String getMessage() {
        return message;
    }

    /**
     * 格式化业务提示消息中的 {} 占位符。
     *
     * @param args 占位符参数
     * @return 格式化后的业务提示消息
     */
    public String formatMessage(Object... args) {
        if (message == null || args == null || args.length == 0) {
            return message;
        }
        String result = message;
        for (Object arg : args) {
            result = result.replaceFirst("\\{\\}", Matcher.quoteReplacement(String.valueOf(arg)));
        }
        return result;
    }
}

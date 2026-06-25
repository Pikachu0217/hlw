package com.hlw.common.core.domain;

public record R<T>(int code, String message, T data) {
    /**
     * 构造成功响应。
     *
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> R<T> ok(T data) {
        return new R<>(200, "success", data);
    }

    /**
     * 构造成功响应。
     *
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> R<T> ok() {
        return new R<>(200, "success", null);
    }

    /**
     * 构造失败响应。
     *
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 失败响应
     */
    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null);
    }
}

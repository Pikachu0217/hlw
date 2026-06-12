package com.hlw.common.core.domain;

public record R<T>(int code, String message, T data) {
    public static <T> R<T> ok(T data) {
        return new R<>(200, "success", data);
    }

    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null);
    }
}

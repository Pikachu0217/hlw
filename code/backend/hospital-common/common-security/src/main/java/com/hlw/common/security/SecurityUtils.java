package com.hlw.common.security;

import cn.dev33.satoken.stp.StpUtil;

public final class SecurityUtils {
    private SecurityUtils() {
    }

    /**
     * 获取当前登录用户编号。
     *
     * @return 登录用户编号
     */
    public static Long getLoginUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    /**
     * 获取当前请求令牌。
     *
     * @return 令牌值
     */
    public static String getTokenValue() {
        return StpUtil.getTokenValue();
    }

    /**
     * 校验当前请求是否已登录。
     */
    public static void checkLogin() {
        StpUtil.checkLogin();
    }

    /**
     * 校验当前用户是否具备权限标识。
     *
     * @param permission 权限标识
     */
    public static void checkPermission(String permission) {
        StpUtil.checkPermission(permission);
    }
}

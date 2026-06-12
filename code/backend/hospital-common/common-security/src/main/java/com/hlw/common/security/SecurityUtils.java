package com.hlw.common.security;

import cn.dev33.satoken.stp.StpUtil;

public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static Long getLoginUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    public static String getTokenValue() {
        return StpUtil.getTokenValue();
    }

    public static void checkLogin() {
        StpUtil.checkLogin();
    }

    public static void checkPermission(String permission) {
        StpUtil.checkPermission(permission);
    }
}

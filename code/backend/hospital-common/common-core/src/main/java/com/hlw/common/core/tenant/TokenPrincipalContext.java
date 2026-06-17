package com.hlw.common.core.tenant;

import com.hlw.common.core.security.TokenPrincipal;

/**
 * 登录令牌主体上下文工具类，用于在多线程环境下存储和获取当前线程的租户信息
 * 使用ThreadLocal实现线程隔离，确保每个线程的登录令牌主体信息独立存储
 */
public final class TokenPrincipalContext {
    // 使用ThreadLocal存储当前线程的租户ID
    private static final ThreadLocal<TokenPrincipal> HOLDER = new ThreadLocal<>();

    /**
     * 私有构造方法，防止实例化工具类
     */
    private TokenPrincipalContext() {
    }

    /**
     * 写入当前线程
     *
     * @param tokenPrincipal 登录令牌主体
     */
    public static void set(TokenPrincipal tokenPrincipal) {
        HOLDER.set(tokenPrincipal);
    }


    /**
     * 获取线程上下文
     */
    public static TokenPrincipal get() {
        return HOLDER.get();
    }

    /**
     * 清理当前线程上下文。
     */
    public static void clear() {
        HOLDER.remove();
    }
}

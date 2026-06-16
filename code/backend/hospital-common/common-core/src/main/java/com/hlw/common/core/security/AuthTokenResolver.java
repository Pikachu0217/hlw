package com.hlw.common.core.security;

/**
 * 公共登录令牌解析器，统一从请求头值中剥离配置化令牌前缀。
 */
public final class AuthTokenResolver {
    /**
     * 私有构造方法，禁止实例化工具类。
     */
    private AuthTokenResolver() {
    }

    /**
     * 从请求头值中提取原始登录令牌。
     *
     * @param tokenHeader 登录令牌请求头值
     * @param tokenPrefix 登录令牌前缀
     * @return 原始登录令牌，缺少令牌时返回 null
     */
    public static String resolve(String tokenHeader, String tokenPrefix) {
        if (tokenHeader == null || tokenHeader.isBlank()) {
            return null;
        }
        String trimmedToken = tokenHeader.trim();
        String normalizedPrefix = tokenPrefix == null ? "" : tokenPrefix.trim();
        String prefixWithSpace = normalizedPrefix + " ";
        if (!normalizedPrefix.isBlank()
                && trimmedToken.regionMatches(true, 0, prefixWithSpace, 0, prefixWithSpace.length())) {
            return trimmedToken.substring(prefixWithSpace.length()).trim();
        }
        return trimmedToken;
    }
}

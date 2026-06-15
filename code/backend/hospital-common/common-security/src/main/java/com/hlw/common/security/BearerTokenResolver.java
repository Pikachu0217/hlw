package com.hlw.common.security;

/**
 * Bearer 登录令牌解析器，统一处理 Authorization 请求头中的令牌值。
 */
public final class BearerTokenResolver {
    /**
     * Authorization 请求头名称。
     */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Bearer 令牌前缀。
     */
    private static final String BEARER_PREFIX = "Bearer";

    /**
     * 私有构造方法，禁止实例化工具类。
     */
    private BearerTokenResolver() {
    }

    /**
     * 从 Authorization 请求头中提取原始登录令牌。
     *
     * @param authorizationHeader Authorization 请求头值
     * @return 原始登录令牌，缺少令牌时返回 null
     */
    public static String resolve(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }
        String trimmed = authorizationHeader.trim();
        String prefixWithSpace = BEARER_PREFIX + " ";
        if (trimmed.regionMatches(true, 0, prefixWithSpace, 0, prefixWithSpace.length())) {
            return trimmed.substring(prefixWithSpace.length()).trim();
        }
        return trimmed;
    }
}

package com.hlw.common.core.security;

import com.hlw.common.core.exception.BizException;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 公共登录令牌解析器，统一从请求头值中剥离配置化令牌前缀。
 */
@Slf4j
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
        if (!StringUtils.hasText(tokenHeader)) {
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

    /**
     * 解析登录请求租户编号，优先使用网关透传请求头。
     *
     * @param tenantHeader 租户请求头
     * @param bodyTenantId 请求体租户编号
     * @return 登录租户编号
     */
    public static Long resolveLoginTenantId(String tenantHeader, Long bodyTenantId) {
        if (tenantHeader == null || tenantHeader.isBlank()) {
            return bodyTenantId;
        }
        try {
            long parsedTenantId = Long.parseLong(tenantHeader.trim());
            if (parsedTenantId < 0L) {
                throw new BizException(400, "租户编号不能小于0");
            }
            return parsedTenantId;
        } catch (NumberFormatException exception) {
            log.warn("登录请求租户请求头格式错误，tenantHeader={}", tenantHeader);
            throw new BizException(400, "租户编号格式错误");
        }
    }
}

package com.hlw.system.service.support;

import com.baomidou.mybatisplus.core.plugins.IgnoreStrategy;
import com.hlw.common.core.constants.CommonConstants;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.core.security.TokenPrincipal;
import com.hlw.common.core.tenant.TokenPrincipalContext;
import lombok.extern.slf4j.Slf4j;

/**
 * MyBatis Plus 与租户上下文的公共工具方法集合，承载跨聚合复用的查询条件、忽略策略与守卫逻辑。
 */
@Slf4j
public final class MybatisTenantHelpers {

    private MybatisTenantHelpers() {
    }

    /**
     * 构造忽略租户行拦截策略，用于平台上下文跨租户读写场景。
     *
     * @return 忽略策略
     */
    public static IgnoreStrategy ignoreTenantLine() {
        return IgnoreStrategy.builder().tenantLine(true).build();
    }

    /**
     * 校验实体是否存在，否则抛出 404 业务异常。
     *
     * @param entity 查询结果
     * @param message 错误消息
     * @param <T> 实体类型
     * @return 非空实体
     */
    public static <T> T requireEntity(T entity, String message) {
        if (entity == null) {
            throw new BizException(404, message);
        }
        return entity;
    }

    /**
     * 校验当前请求是否处于平台上下文，否则抛出 403 业务异常。
     *
     * @param message 不满足条件时的错误消息
     */
    public static void ensurePlatformContext(String message) {
        TokenPrincipal principal = TokenPrincipalContext.get();
        Long tenantId = principal == null ? null : principal.getTenantId();
        if (!CommonConstants.isPlatformTenant(tenantId)) {
            log.warn("平台租户上下文校验失败，tenantId={}，message={}", tenantId, message);
            throw new BizException(403, message);
        }
    }

    /**
     * 获取当前请求租户编号。
     *
     * @return 当前请求租户编号
     */
    public static Long currentTenantId() {
        TokenPrincipal principal = TokenPrincipalContext.get();
        Long tenantId = principal == null ? null : principal.getTenantId();
        if (tenantId == null || CommonConstants.ISOLATED_TENANT_ID == tenantId) {
            log.warn("当前租户上下文无效，tenantId={}", tenantId);
            throw new BizException(403, "租户上下文无效");
        }
        return tenantId;
    }

    /**
     * 获取当前请求租户编号字符串。
     *
     * @return 当前请求租户编号字符串
     */
    public static String currentTenantIdString() {
        return String.valueOf(currentTenantId());
    }
}

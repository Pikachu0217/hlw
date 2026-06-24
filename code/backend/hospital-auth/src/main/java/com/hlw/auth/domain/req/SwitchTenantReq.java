package com.hlw.auth.domain.req;

import jakarta.validation.constraints.NotNull;

/**
 * 切换登录租户请求。
 *
 * @param tenantId 目标租户编号
 */
public record SwitchTenantReq(
        @NotNull(message = "目标租户编号不能为空")
        Long tenantId) {
}

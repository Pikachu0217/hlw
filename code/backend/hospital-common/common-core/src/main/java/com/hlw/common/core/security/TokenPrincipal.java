package com.hlw.common.core.security;

import lombok.Data;

/**
 * 登录令牌主体。
 */
@Data
public class TokenPrincipal {
    /**
     * 租户 id
     */
    private Long tenantId;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 是否为平台请求的标记
     */
    private Boolean platformRequest;
}

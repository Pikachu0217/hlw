package com.hlw.common.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 公共认证令牌配置属性，统一描述网关、认证服务和业务服务识别登录令牌的协议。
 */
@Component
@ConfigurationProperties(prefix = "hlw.auth")
public class AuthTokenProperties {
    /**
     * 登录令牌请求头名称。
     */
    private String tokenName = "Authorization";

    /**
     * 登录令牌前缀。
     */
    private String tokenPrefix = "Bearer";

    /**
     * 网关透传的可信租户请求头名称。
     */
    private String tenantHeaderName = "X-Tenant-Id";

    /**
     * 获取登录令牌请求头名称。
     *
     * @return 登录令牌请求头名称
     */
    public String getTokenName() {
        return tokenName;
    }

    /**
     * 设置登录令牌请求头名称。
     *
     * @param tokenName 登录令牌请求头名称
     */
    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    /**
     * 获取登录令牌前缀。
     *
     * @return 登录令牌前缀
     */
    public String getTokenPrefix() {
        return tokenPrefix;
    }

    /**
     * 设置登录令牌前缀。
     *
     * @param tokenPrefix 登录令牌前缀
     */
    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }

    /**
     * 获取可信租户请求头名称。
     *
     * @return 可信租户请求头名称
     */
    public String getTenantHeaderName() {
        return tenantHeaderName;
    }

    /**
     * 设置可信租户请求头名称。
     *
     * @param tenantHeaderName 可信租户请求头名称
     */
    public void setTenantHeaderName(String tenantHeaderName) {
        this.tenantHeaderName = tenantHeaderName;
    }
}

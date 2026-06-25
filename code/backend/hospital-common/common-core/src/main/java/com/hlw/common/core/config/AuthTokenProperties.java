package com.hlw.common.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 公共认证令牌配置属性，统一描述网关、认证服务和业务服务识别登录令牌的协议。
 */
@Component
@ConfigurationProperties(prefix = "hlw.auth")
@Getter
@Setter
public class AuthTokenProperties {
    /**
     * 登录令牌请求头名称。
     */
    private String tokenName;

    /**
     * 登录令牌前缀。
     */
    private String tokenPrefix;

    /**
     * 网关透传的可信租户请求头名称。
     */
    private String tenantHeaderName;

    /**
     * 子模块直连时可选透传的系统用户编号请求头名称。
     */
    private String userHeaderName = "X-User-Id";

    /**
     * 子模块直连时可选透传的业务用户编号请求头名称。
     */
    private String businessUserHeaderName = "X-Business-User-Id";

    /**
     * 子模块直连时可选透传的用户类型请求头名称。
     */
    private String userTypeHeaderName = "X-User-Type";

}

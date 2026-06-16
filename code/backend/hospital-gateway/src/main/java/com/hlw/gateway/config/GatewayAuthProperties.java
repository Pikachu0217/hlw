package com.hlw.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关认证配置属性。
 */
@Component
@ConfigurationProperties(prefix = "hlw.gateway")
@Data
public class GatewayAuthProperties {
    /**
     * 网关放行的公开接口路径。
     */
    private List<String> publicPaths = new ArrayList<>();
}

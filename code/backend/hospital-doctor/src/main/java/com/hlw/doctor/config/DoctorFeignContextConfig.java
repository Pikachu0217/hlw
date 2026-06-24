package com.hlw.doctor.config;

import com.hlw.common.core.config.AuthTokenProperties;
import com.hlw.common.core.tenant.TokenPrincipalContext;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 医生服务 Feign 上下文透传配置。
 */
@Configuration
public class DoctorFeignContextConfig {

    /**
     * 创建租户请求头透传拦截器。
     *
     * @param authTokenProperties 公共认证令牌配置属性
     * @return Feign 请求拦截器
     */
    @Bean
    public RequestInterceptor doctorTenantFeignRequestInterceptor(AuthTokenProperties authTokenProperties) {
        return template -> {
            var principal = TokenPrincipalContext.get();
            if (principal != null && principal.getTenantId() != null) {
                template.header(authTokenProperties.getTenantHeaderName(), String.valueOf(principal.getTenantId()));
            }
        };
    }
}

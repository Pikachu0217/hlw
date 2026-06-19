package com.hlw.system.config;

import com.hlw.system.web.SystemOperatorLogInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 系统模块 Web MVC 配置。
 */
@Configuration
@RequiredArgsConstructor
public class SystemWebMvcConfig implements WebMvcConfigurer {
    /** 系统操作日志拦截器。 */
    private final SystemOperatorLogInterceptor systemOperatorLogInterceptor;

    /**
     * 注册系统模块拦截器。
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(systemOperatorLogInterceptor)
            .addPathPatterns("/system/**")
            .excludePathPatterns("/system/log/**");
    }
}

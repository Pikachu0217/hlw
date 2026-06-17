package com.hlw.common.mybatis.datascope.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.hlw.common.mybatis.datascope.interceptor.DataScopeAnnotationResolver;
import com.hlw.common.mybatis.datascope.interceptor.HlwDataPermissionHandler;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 数据权限组件自动装配。
 *
 * <p>这里只注册数据权限元数据解析器和处理器；真正的 {@link DataPermissionInterceptor}
 * 在 {@code MybatisPlusTenantConfig} 创建 {@link MybatisPlusInterceptor} 时同步加入拦截器链，
 * 避免 {@code SqlSessionFactory} 初始化后再修改插件链导致不生效。</p>
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({MybatisPlusInterceptor.class, DataPermissionInterceptor.class})
@AutoConfigureBefore(name = "com.hlw.common.mybatis.config.MybatisPlusTenantConfig")
public class DataScopeAutoConfiguration {

    @Bean
    public DataScopeAnnotationResolver dataScopeAnnotationResolver() {
        return new DataScopeAnnotationResolver();
    }

    @Bean
    public HlwDataPermissionHandler hlwDataPermissionHandler(DataScopeAnnotationResolver resolver) {
        return new HlwDataPermissionHandler(resolver);
    }
}

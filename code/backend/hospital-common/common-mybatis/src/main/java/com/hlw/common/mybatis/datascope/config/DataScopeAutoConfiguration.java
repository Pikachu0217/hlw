package com.hlw.common.mybatis.datascope.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.hlw.common.mybatis.datascope.interceptor.DataScopeAnnotationResolver;
import com.hlw.common.mybatis.datascope.interceptor.HlwDataPermissionHandler;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 数据权限拦截器自动装配。
 *
 * <p>在 {@code MybatisPlusTenantConfig} 注册的既有 {@link MybatisPlusInterceptor} Bean 上追加
 * 一个 {@link DataPermissionInterceptor}，确保责任链最终顺序为：
 * Pagination → TenantLine → DataPermission。</p>
 *
 * <p>使用 {@link SmartInitializingSingleton} 在所有单例就绪后追加；不在 {@code @PostConstruct} 阶段
 * 修改 {@link MybatisPlusInterceptor}，避免与 SqlSessionFactory 的初始化时序耦合。</p>
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({MybatisPlusInterceptor.class, DataPermissionInterceptor.class})
@AutoConfigureAfter(name = "com.hlw.common.mybatis.config.MybatisPlusTenantConfig")
public class DataScopeAutoConfiguration {

    @Bean
    public DataScopeAnnotationResolver dataScopeAnnotationResolver() {
        return new DataScopeAnnotationResolver();
    }

    @Bean
    public HlwDataPermissionHandler hlwDataPermissionHandler(DataScopeAnnotationResolver resolver) {
        return new HlwDataPermissionHandler(resolver);
    }

    @Bean
    public SmartInitializingSingleton dataPermissionInterceptorRegistrar(MybatisPlusInterceptor mybatisPlusInterceptor,
                                                                         HlwDataPermissionHandler handler) {
        return () -> {
            List<InnerInterceptor> chain = mybatisPlusInterceptor.getInterceptors();
            for (InnerInterceptor existing : chain) {
                if (existing instanceof DataPermissionInterceptor) {
                    log.info("DataPermissionInterceptor already present, skip registration");
                    return;
                }
            }
            mybatisPlusInterceptor.addInnerInterceptor(new DataPermissionInterceptor(handler));
            log.info("DataPermissionInterceptor registered, chain size={}",
                    mybatisPlusInterceptor.getInterceptors().size());
        };
    }
}

package com.hlw.common.mybatis.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.hlw.common.mybatis.datascope.interceptor.HlwDataPermissionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusTenantConfig {
    private static final long DEFAULT_PAGE_MAX_LIMIT = 500L;

    /**
     * 创建 MyBatis Plus 拦截器，按租户、数据权限、分页的顺序注册。
     *
     * @param dataPermissionHandler 数据权限处理器
     * @return MyBatis Plus 拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(HlwDataPermissionHandler dataPermissionHandler) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new HlwTenantLineHandler()));
        interceptor.addInnerInterceptor(new DataPermissionInterceptor(dataPermissionHandler));
        PaginationInnerInterceptor pageInterceptor = new PaginationInnerInterceptor(DbType.POSTGRE_SQL);
        pageInterceptor.setMaxLimit(DEFAULT_PAGE_MAX_LIMIT);
        pageInterceptor.setOverflow(false);
        interceptor.addInnerInterceptor(pageInterceptor);
        return interceptor;
    }
}

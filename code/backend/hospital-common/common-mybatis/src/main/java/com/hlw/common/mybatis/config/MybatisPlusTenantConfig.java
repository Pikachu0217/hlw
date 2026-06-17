package com.hlw.common.mybatis.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusTenantConfig {
    private static final long DEFAULT_PAGE_MAX_LIMIT = 500L;

    /**
     * 创建 MyBatis Plus 拦截器，按 MP 推荐顺序先注册分页再注册租户拦截器。
     *
     * @return MyBatis Plus 拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor pageInterceptor = new PaginationInnerInterceptor(DbType.POSTGRE_SQL);
        pageInterceptor.setMaxLimit(DEFAULT_PAGE_MAX_LIMIT);
        pageInterceptor.setOverflow(false);
        interceptor.addInnerInterceptor(pageInterceptor);
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new HlwTenantLineHandler()));
        return interceptor;
    }
}

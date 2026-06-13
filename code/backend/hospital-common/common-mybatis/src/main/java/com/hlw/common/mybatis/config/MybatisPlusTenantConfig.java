package com.hlw.common.mybatis.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.hlw.common.core.tenant.TenantContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusTenantConfig {
    /**
     * 创建 MyBatis Plus 租户拦截器。
     *
     * @return MyBatis Plus 拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new HlwTenantLineHandler()));
        return interceptor;
    }

    private static final class HlwTenantLineHandler implements com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler {
        /**
         * 获取当前租户表达式。
         *
         * @return 租户表达式
         */
        @Override
        public Expression getTenantId() {
            Long tenantId = TenantContext.getTenantId();
            return new LongValue(tenantId == null ? 0L : tenantId);
        }

        /**
         * 判断表是否忽略租户过滤。
         *
         * @param tableName 表名
         * @return 是否忽略
         */
        @Override
        public boolean ignoreTable(String tableName) {
            return "local_message".equalsIgnoreCase(tableName);
        }
    }
}

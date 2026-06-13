package com.hlw.order.config;

import com.hlw.common.core.jdbc.DemoDataQuery;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;

/**
 * 订单模块数据库查询配置。
 */
@Configuration
public class OrderDataQueryConfig {
    /**
     * 创建订单模块演示数据查询器。
     *
     * @param jdbcOperations JDBC 操作组件
     * @return 演示数据查询器
     */
    @Bean
    public DemoDataQuery demoDataQuery(JdbcOperations jdbcOperations) {
        return new DemoDataQuery(jdbcOperations);
    }
}

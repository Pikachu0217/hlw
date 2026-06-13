package com.hlw.prescription.config;

import com.hlw.common.core.jdbc.DemoDataQuery;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;

/**
 * 处方模块数据库查询配置。
 */
@Configuration
public class PrescriptionDataQueryConfig {
    /**
     * 创建处方模块演示数据查询器。
     *
     * @param jdbcOperations JDBC 操作组件
     * @return 演示数据查询器
     */
    @Bean
    public DemoDataQuery demoDataQuery(JdbcOperations jdbcOperations) {
        return new DemoDataQuery(jdbcOperations);
    }
}

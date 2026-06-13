package com.hlw.common.core.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;

import java.util.List;
import java.util.Map;

/**
 * 演示数据查询器，统一承载从数据库读取示例列表数据的能力。
 */
public class DemoDataQuery {
    private static final Logger log = LoggerFactory.getLogger(DemoDataQuery.class);

    private final JdbcOperations jdbcOperations;

    /**
     * 构造演示数据查询器。
     *
     * @param jdbcOperations JDBC 操作组件
     */
    public DemoDataQuery(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    /**
     * 查询演示列表数据。
     *
     * @param scene 查询场景名称
     * @param sql 查询 SQL
     * @return 演示列表数据
     */
    public List<Map<String, Object>> list(String scene, String sql) {
        log.info("查询数据库演示列表，scene={}", scene);
        List<Map<String, Object>> rows = jdbcOperations.queryForList(sql);
        log.info("数据库演示列表查询完成，scene={}，count={}", scene, rows.size());
        return rows;
    }

    /**
     * 查询演示列表数据。
     *
     * @param scene 查询场景名称
     * @param sql 查询 SQL
     * @param args 查询参数
     * @return 演示列表数据
     */
    public List<Map<String, Object>> list(String scene, String sql, Object... args) {
        log.info("查询数据库演示列表，scene={}", scene);
        List<Map<String, Object>> rows = jdbcOperations.queryForList(sql, args);
        log.info("数据库演示列表查询完成，scene={}，count={}", scene, rows.size());
        return rows;
    }

    /**
     * 查询单条演示数据。
     *
     * @param scene 查询场景名称
     * @param sql 查询 SQL
     * @param args 查询参数
     * @return 单条演示数据，不存在时返回空 Map
     */
    public Map<String, Object> one(String scene, String sql, Object... args) {
        log.info("查询数据库演示详情，scene={}", scene);
        List<Map<String, Object>> rows = jdbcOperations.queryForList(sql, args);
        if (rows.isEmpty()) {
            log.info("数据库演示详情不存在，scene={}", scene);
            return Map.of();
        }
        log.info("数据库演示详情查询完成，scene={}", scene);
        return rows.get(0);
    }
}

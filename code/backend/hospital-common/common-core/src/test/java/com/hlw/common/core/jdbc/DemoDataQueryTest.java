package com.hlw.common.core.jdbc;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcOperations;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DemoDataQueryTest {
    /**
     * 验证演示数据查询器直接透传数据库查询结果，避免控制器继续保留写死数据。
     */
    @Test
    void list_returns_rows_from_jdbc_query() {
        JdbcOperations jdbcOperations = mock(JdbcOperations.class);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("key", "1");
        row.put("doctorName", "陈知衡");
        String sql = "select id::text as key, doctor_name as doctorName from doc_doctor";
        when(jdbcOperations.queryForList(sql)).thenReturn(List.of(row));

        DemoDataQuery query = new DemoDataQuery(jdbcOperations);

        List<Map<String, Object>> rows = query.list("医生列表", sql);

        assertThat(rows).containsExactly(row);
        verify(jdbcOperations).queryForList(sql);
    }

    /**
     * 验证演示列表查询支持查询参数。
     */
    @Test
    void list_accepts_query_arguments() {
        JdbcOperations jdbcOperations = mock(JdbcOperations.class);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("content", "哪里不舒服");
        String sql = "select content from con_message where consult_id = ?";
        when(jdbcOperations.queryForList(sql, 1L)).thenReturn(List.of(row));

        DemoDataQuery query = new DemoDataQuery(jdbcOperations);

        List<Map<String, Object>> rows = query.list("问诊消息", sql, 1L);

        assertThat(rows).containsExactly(row);
        verify(jdbcOperations).queryForList(sql, 1L);
    }

    /**
     * 验证单行查询按参数访问数据库，并在没有数据时返回空 Map。
     */
    @Test
    void one_returns_empty_map_when_no_row_found() {
        JdbcOperations jdbcOperations = mock(JdbcOperations.class);
        String sql = "select id::text as key from doc_doctor where id = ?";
        when(jdbcOperations.queryForList(sql, 99L)).thenReturn(List.of());

        DemoDataQuery query = new DemoDataQuery(jdbcOperations);

        Map<String, Object> row = query.one("医生详情", sql, 99L);

        assertThat(row).isEmpty();
        verify(jdbcOperations).queryForList(sql, 99L);
    }
}

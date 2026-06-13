package com.hlw.common.core.schema;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresInitSqlTest {
    @Test
    void postgres_init_sql_exists_inside_backend_project() throws Exception {
        Path path = Path.of("/Users/pakachuzy/Desktop/zzz/project/hlw/code/backend/sql/init.sql");

        assertThat(Files.exists(path)).isTrue();
        String sql = Files.readString(path);
        assertThat(sql).contains("CREATE TABLE IF NOT EXISTS sys_user");
        assertThat(sql).contains("CREATE TABLE IF NOT EXISTS con_consult");
        assertThat(sql).contains("CREATE TABLE IF NOT EXISTS ord_order");
        assertThat(sql).contains("CREATE TABLE IF NOT EXISTS local_message");
        assertThat(sql).contains("CREATE TABLE IF NOT EXISTS doc_department");
        assertThat(sql).contains("CREATE TABLE IF NOT EXISTS doc_schedule");
        assertThat(sql).contains("CREATE TABLE IF NOT EXISTS apt_number_source");
        assertThat(sql).contains("CREATE TABLE IF NOT EXISTS con_message");
        assertThat(sql).contains("CREATE TABLE IF NOT EXISTS drug_stock");
        assertThat(sql).contains("CREATE TABLE IF NOT EXISTS pat_health_record");
        assertThat(sql).contains("INSERT INTO doc_doctor");
        assertThat(sql).contains("INSERT INTO sys_user");
        assertThat(sql).contains("COMMENT ON COLUMN drug_stock.warehouse_name");
    }
}

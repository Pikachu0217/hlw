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
    }
}

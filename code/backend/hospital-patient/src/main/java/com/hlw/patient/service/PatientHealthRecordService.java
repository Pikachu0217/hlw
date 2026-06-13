package com.hlw.patient.service;

import com.hlw.common.core.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 患者健康档案服务，负责健康档案创建和患者存在性校验。
 */
@Service
public class PatientHealthRecordService {
    private static final Logger log = LoggerFactory.getLogger(PatientHealthRecordService.class);
    private static final long DEFAULT_TENANT_ID = 100L;
    private static final long DEFAULT_PATIENT_ID = 1L;

    private final JdbcOperations jdbcOperations;

    /**
     * 构造患者健康档案服务。
     *
     * @param jdbcOperations JDBC 操作组件
     */
    public PatientHealthRecordService(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    /**
     * 创建健康档案。
     *
     * @param command 健康档案创建命令
     * @return 创建后的健康档案数据
     */
    @Transactional
    public Map<String, Object> createHealthRecord(Map<String, Object> command) {
        Long patientId = longValue(command, "patientId", DEFAULT_PATIENT_ID);
        String title = requiredString(command, "title", "档案标题不能为空");
        String summary = requiredString(command, "summary", "档案摘要不能为空");
        log.info("创建健康档案，patientId={}，title={}", patientId, title);
        assertPatientExists(patientId);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator creator = connection -> {
            PreparedStatement statement = connection.prepareStatement(
                """
                    INSERT INTO pat_health_record (tenant_id, patient_id, title, summary)
                    VALUES (?, ?, ?, ?)
                    """,
                new String[]{"id"}
            );
            statement.setLong(1, DEFAULT_TENANT_ID);
            statement.setLong(2, patientId);
            statement.setString(3, title);
            statement.setString(4, summary);
            return statement;
        };
        jdbcOperations.update(creator, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new BizException(500, "健康档案创建后未返回主键");
        }

        return row(
            "id", key.longValue(),
            "patientId", patientId,
            "title", title,
            "summary", summary
        );
    }

    /**
     * 校验患者是否存在。
     *
     * @param patientId 患者编号
     */
    private void assertPatientExists(Long patientId) {
        Integer count = jdbcOperations.queryForObject(
            "SELECT COUNT(1) FROM pat_patient WHERE id = ? AND deleted = 0",
            Integer.class,
            patientId
        );
        if (count == null || count == 0) {
            throw new BizException(404, "患者不存在");
        }
    }

    /**
     * 读取必填字符串。
     *
     * @param command 请求命令
     * @param key 字段名
     * @param message 错误消息
     * @return 字段值
     */
    private String requiredString(Map<String, Object> command, String key, String message) {
        String value = stringValue(command, key, "");
        if (value.isBlank()) {
            throw new BizException(400, message);
        }
        return value;
    }

    /**
     * 读取可选字符串。
     *
     * @param command 请求命令
     * @param key 字段名
     * @param defaultValue 默认值
     * @return 字段值
     */
    private String stringValue(Map<String, Object> command, String key, String defaultValue) {
        Object value = command.get(key);
        if (value == null) {
            return defaultValue;
        }
        return Objects.toString(value).trim();
    }

    /**
     * 读取长整型字段。
     *
     * @param command 请求命令
     * @param key 字段名
     * @param defaultValue 默认值
     * @return 字段值
     */
    private Long longValue(Map<String, Object> command, String key, Long defaultValue) {
        Object value = command.get(key);
        if (value == null || Objects.toString(value).isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(Objects.toString(value));
        } catch (NumberFormatException ex) {
            throw new BizException(400, key + "必须是数字");
        }
    }

    /**
     * 构造有序返回行。
     *
     * @param values 成对键值
     * @return 有序 Map
     */
    private Map<String, Object> row(Object... values) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int index = 0; index < values.length; index += 2) {
            row.put(Objects.toString(values[index]), values[index + 1]);
        }
        return row;
    }
}

package com.hlw.doctor.service;

import com.hlw.common.core.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 医生科室管理服务，负责科室资料维护和医生科室绑定。
 */
@Service
public class DoctorDepartmentService {
    private static final Logger log = LoggerFactory.getLogger(DoctorDepartmentService.class);
    private static final long DEFAULT_TENANT_ID = 100L;

    private final JdbcOperations jdbcOperations;

    /**
     * 构造医生科室管理服务。
     *
     * @param jdbcOperations JDBC 操作组件
     */
    public DoctorDepartmentService(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    /**
     * 查询科室列表，并实时汇总医生绑定数量。
     *
     * @return 科室列表
     */
    public List<Map<String, Object>> listDepartments() {
        log.info("查询科室列表");
        return jdbcOperations.queryForList("""
            SELECT d.id AS id,
                   d.id::text AS key,
                   COALESCE(NULLIF(d.department_name, ''), d.name) AS name,
                   CASE
                       WHEN COUNT(DISTINCT dd.doctor_id) = 0 THEN d.doctor_count
                       ELSE COUNT(DISTINCT dd.doctor_id)
                   END::int AS "doctorCount",
                   d.queue_desc AS queue,
                   d.status AS status
            FROM doc_department d
            LEFT JOIN doc_doctor_department dd ON dd.department_id = d.id AND dd.deleted = 0
            WHERE d.deleted = 0
            GROUP BY d.id, d.department_name, d.name, d.doctor_count, d.queue_desc, d.status
            ORDER BY d.sort, d.id
            """);
    }

    /**
     * 创建科室。
     *
     * @param command 科室创建命令
     * @return 创建后的科室数据
     */
    @Transactional
    public Map<String, Object> createDepartment(Map<String, Object> command) {
        String name = requiredString(command, "name", "科室名称不能为空");
        int sort = intValue(command, "sort", 0);
        String status = stringValue(command, "status", "启用");
        String queue = stringValue(command, "queue", "当前等候 0 人");
        String description = stringValue(command, "description", "");
        Long parentId = longValue(command, "parentId", 0L);
        log.info("创建科室，name={}，parentId={}", name, parentId);
        Long id = nextSequenceValue("doc_department");
        jdbcOperations.update("""
            INSERT INTO doc_department (
                id, tenant_id, name, department_name, parent_id, sort, status, queue_desc, doctor_count, description
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, ?)
            """, id, DEFAULT_TENANT_ID, name, name, parentId, sort, status, queue, description);
        return row("id", id, "key", String.valueOf(id), "name", name, "doctorCount", 0, "queue", queue, "status", status);
    }

    /**
     * 绑定医生和科室。
     *
     * @param doctorId 医生编号
     * @param command 绑定命令
     * @return 绑定结果
     */
    @Transactional
    public Map<String, Object> bindDoctorDepartment(Long doctorId, Map<String, Object> command) {
        Long departmentId = requiredLong(command, "departmentId", "科室编号不能为空");
        boolean free = booleanValue(command, "free", false);
        BigDecimal appointmentFee = decimalValue(command, "appointmentFee", BigDecimal.ZERO);
        log.info("绑定医生科室，doctorId={}，departmentId={}", doctorId, departmentId);
        assertExists("doc_doctor", doctorId, "医生不存在");
        assertExists("doc_department", departmentId, "科室不存在");
        Long id = nextSequenceValue("doc_doctor_department");
        jdbcOperations.update("""
            INSERT INTO doc_doctor_department (id, tenant_id, doctor_id, department_id, is_free, appointment_fee)
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT (doctor_id, department_id) WHERE deleted = 0
            DO UPDATE SET update_time = CURRENT_TIMESTAMP
            """, id, DEFAULT_TENANT_ID, doctorId, departmentId, free ? 1 : 0, appointmentFee);
        refreshDepartmentDoctorCount(departmentId);
        return findDoctorDepartment(doctorId, departmentId);
    }

    /**
     * 查询医生科室已存在绑定。
     *
     * @param doctorId 医生编号
     * @param departmentId 科室编号
     * @return 已存在绑定，不存在返回空 Map
     */
    private Map<String, Object> findDoctorDepartment(Long doctorId, Long departmentId) {
        List<Map<String, Object>> rows = jdbcOperations.queryForList("""
            SELECT id::text AS key,
                   doctor_id AS "doctorId",
                   department_id AS "departmentId",
                   CASE WHEN is_free = 1 THEN true ELSE false END AS free,
                   appointment_fee AS "appointmentFee"
            FROM doc_doctor_department
            WHERE doctor_id = ? AND department_id = ? AND deleted = 0
            ORDER BY id
            LIMIT 1
            """, doctorId, departmentId);
        return rows.isEmpty() ? Map.of() : rows.get(0);
    }

    /**
     * 刷新科室医生数量冗余字段，兼容列表展示和旧数据。
     *
     * @param departmentId 科室编号
     */
    private void refreshDepartmentDoctorCount(Long departmentId) {
        jdbcOperations.update("""
            UPDATE doc_department
            SET doctor_count = (
                    SELECT COUNT(DISTINCT doctor_id)
                    FROM doc_doctor_department
                    WHERE department_id = ? AND deleted = 0
                ),
                update_time = CURRENT_TIMESTAMP
            WHERE id = ? AND deleted = 0
            """, departmentId, departmentId);
    }

    /**
     * 校验指定表的记录是否存在。
     *
     * @param tableName 表名
     * @param id 主键编号
     * @param message 不存在时的错误消息
     */
    private void assertExists(String tableName, Long id, String message) {
        if (!List.of("doc_doctor", "doc_department").contains(tableName)) {
            throw new BizException(500, "医生服务表未纳入白名单");
        }
        Integer count = jdbcOperations.queryForObject(
            "SELECT COUNT(1) FROM " + tableName + " WHERE id = ? AND deleted = 0",
            Integer.class,
            id
        );
        if (count == null || count == 0) {
            throw new BizException(404, message);
        }
    }

    /**
     * 从 PostgreSQL 序列获取指定表的下一个主键。
     *
     * @param tableName 表名
     * @return 下一个主键
     */
    private Long nextSequenceValue(String tableName) {
        if (!List.of("doc_department", "doc_doctor_department").contains(tableName)) {
            throw new BizException(500, "医生服务主键表未纳入白名单");
        }
        synchronizeSequence(tableName);
        return jdbcOperations.queryForObject(
            "SELECT nextval(pg_get_serial_sequence('" + tableName + "', 'id'))",
            Long.class
        );
    }

    /**
     * 将表主键序列同步到当前最大主键，兼容初始化脚本中的显式主键种子数据。
     *
     * @param tableName 表名
     */
    private void synchronizeSequence(String tableName) {
        jdbcOperations.queryForObject(
            "SELECT setval(pg_get_serial_sequence('" + tableName + "', 'id'), "
                + "GREATEST((SELECT COALESCE(MAX(id), 0) FROM " + tableName + "), 1), true)",
            Long.class
        );
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
     * 读取必填长整型。
     *
     * @param command 请求命令
     * @param key 字段名
     * @param message 错误消息
     * @return 字段值
     */
    private Long requiredLong(Map<String, Object> command, String key, String message) {
        Object value = command.get(key);
        if (value == null || Objects.toString(value).isBlank()) {
            throw new BizException(400, message);
        }
        try {
            return Long.parseLong(Objects.toString(value));
        } catch (NumberFormatException ex) {
            throw new BizException(400, message);
        }
    }

    /**
     * 读取可选长整型。
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
     * 读取整型字段。
     *
     * @param command 请求命令
     * @param key 字段名
     * @param defaultValue 默认值
     * @return 字段值
     */
    private int intValue(Map<String, Object> command, String key, int defaultValue) {
        Object value = command.get(key);
        if (value == null || Objects.toString(value).isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(Objects.toString(value));
        } catch (NumberFormatException ex) {
            throw new BizException(400, key + "必须是数字");
        }
    }

    /**
     * 读取布尔字段。
     *
     * @param command 请求命令
     * @param key 字段名
     * @param defaultValue 默认值
     * @return 字段值
     */
    private boolean booleanValue(Map<String, Object> command, String key, boolean defaultValue) {
        Object value = command.get(key);
        if (value == null || Objects.toString(value).isBlank()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(Objects.toString(value));
    }

    /**
     * 读取金额字段。
     *
     * @param command 请求命令
     * @param key 字段名
     * @param defaultValue 默认值
     * @return 字段值
     */
    private BigDecimal decimalValue(Map<String, Object> command, String key, BigDecimal defaultValue) {
        Object value = command.get(key);
        if (value == null || Objects.toString(value).isBlank()) {
            return defaultValue;
        }
        try {
            return new BigDecimal(Objects.toString(value));
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
        for (int i = 0; i < values.length; i += 2) {
            row.put(Objects.toString(values[i]), values[i + 1]);
        }
        return row;
    }
}

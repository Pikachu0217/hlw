package com.hlw.appointment.service;

import com.hlw.common.core.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 预约工作流服务，负责预约单、号源锁定和放号配置落库。
 */
@Service
public class AppointmentWorkflowService {
    private static final Logger log = LoggerFactory.getLogger(AppointmentWorkflowService.class);
    private static final long DEFAULT_TENANT_ID = 100L;
    private static final long DEFAULT_PATIENT_ID = 1L;
    private static final long DEFAULT_DOCTOR_ID = 1L;
    private static final long DEFAULT_DEPARTMENT_ID = 10L;
    private static final long DEFAULT_SCHEDULE_ID = 1L;

    private final JdbcOperations jdbcOperations;

    /**
     * 构造预约工作流服务。
     *
     * @param jdbcOperations JDBC 操作组件
     */
    public AppointmentWorkflowService(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    /**
     * 创建预约单。
     *
     * @param command 预约创建命令
     * @return 创建后的预约单
     */
    @Transactional
    public Map<String, Object> createAppointment(Map<String, Object> command) {
        Long patientId = longValue(command, "patientId", DEFAULT_PATIENT_ID);
        Long doctorId = longValue(command, "doctorId", DEFAULT_DOCTOR_ID);
        Long departmentId = longValue(command, "departmentId", DEFAULT_DEPARTMENT_ID);
        Long scheduleId = longValue(command, "scheduleId", DEFAULT_SCHEDULE_ID);
        String doctorName = stringValue(command, "doctorName", "陈知衡");
        String patientName = stringValue(command, "patientName", "赵晓岚");
        String clinicTime = stringValue(command, "timeSlot", "2026-06-13 上午");
        String source = stringValue(command, "source", "小程序");
        String appointmentType = stringValue(command, "appointmentType", "普通门诊");
        BigDecimal feeAmount = decimalValue(command, "feeAmount", new BigDecimal("30"));
        log.info("创建预约单，patientId={}，doctorId={}，scheduleId={}", patientId, doctorId, scheduleId);

        Long numberSourceId = lockAvailableNumberSource(scheduleId);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator creator = connection -> {
            PreparedStatement statement = connection.prepareStatement(
                """
                    INSERT INTO apt_appointment (
                        tenant_id, patient_id, doctor_id, department_id, schedule_id, number_source_id,
                        appointment_type, appointment_no, patient_name, doctor_name, clinic_time, source, status, fee_amount
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, '', ?, ?, ?, ?, '待支付', ?)
                    """,
                new String[]{"id"}
            );
            statement.setLong(1, DEFAULT_TENANT_ID);
            statement.setLong(2, patientId);
            statement.setLong(3, doctorId);
            statement.setLong(4, departmentId);
            statement.setLong(5, scheduleId);
            statement.setLong(6, numberSourceId);
            statement.setString(7, appointmentType);
            statement.setString(8, patientName);
            statement.setString(9, doctorName);
            statement.setString(10, clinicTime);
            statement.setString(11, source);
            statement.setBigDecimal(12, feeAmount);
            return statement;
        };
        jdbcOperations.update(creator, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new BizException(500, "预约单创建后未返回主键");
        }
        long appointmentId = key.longValue();
        String appointmentNo = "YY20260613" + String.format("%04d", appointmentId);
        jdbcOperations.update(
            "UPDATE apt_appointment SET appointment_no = ?, update_time = CURRENT_TIMESTAMP WHERE id = ?",
            appointmentNo,
            appointmentId
        );
        return row(
            "id", appointmentId,
            "appointmentNo", appointmentNo,
            "patientName", patientName,
            "doctorName", doctorName,
            "clinicTime", clinicTime,
            "source", source,
            "status", "待支付"
        );
    }

    /**
     * 支付预约单。
     *
     * @param id 预约单编号
     * @return 支付结果
     */
    @Transactional
    public Map<String, Object> pay(Long id) {
        log.info("支付预约单，appointmentId={}", id);
        String status = queryAppointmentStatus(id);
        if ("已支付".equals(status) || "已签到".equals(status) || "已完成".equals(status)) {
            log.info("预约单无需重复支付，appointmentId={}，status={}", id, status);
            return row("id", id, "status", status);
        }
        if (!"待支付".equals(status)) {
            throw new BizException(409, "预约单当前状态不允许支付");
        }
        jdbcOperations.update(
            """
                UPDATE apt_appointment
                SET status = '已支付',
                    pay_time = CURRENT_TIMESTAMP,
                    update_time = CURRENT_TIMESTAMP
                WHERE id = ? AND deleted = 0
                """,
            id
        );
        return row("id", id, "status", "已支付");
    }

    /**
     * 预约签到。
     *
     * @param id 预约单编号
     * @return 签到结果
     */
    @Transactional
    public Map<String, Object> checkIn(Long id) {
        log.info("预约签到，appointmentId={}", id);
        String status = queryAppointmentStatus(id);
        if ("已签到".equals(status) || "已完成".equals(status)) {
            log.info("预约单无需重复签到，appointmentId={}，status={}", id, status);
            return row("id", id, "status", status);
        }
        if (!"已支付".equals(status)) {
            throw new BizException(409, "预约单当前状态不允许签到");
        }
        jdbcOperations.update(
            """
                UPDATE apt_appointment
                SET status = '已签到',
                    check_in_time = CURRENT_TIMESTAMP,
                    update_time = CURRENT_TIMESTAMP
                WHERE id = ? AND deleted = 0
                """,
            id
        );
        return row("id", id, "status", "已签到");
    }

    /**
     * 抢便民门诊预约单。
     *
     * @param id 预约单编号
     * @param doctorId 医生编号
     * @return 抢单是否成功
     */
    @Transactional
    public Boolean grab(Long id, Long doctorId) {
        if (doctorId == null) {
            throw new BizException(400, "医生编号不能为空");
        }
        log.info("抢便民门诊预约单，appointmentId={}，doctorId={}", id, doctorId);
        String status = queryAppointmentStatus(id);
        if ("已签到".equals(status) || "已接单".equals(status)) {
            log.info("预约单无需重复抢单，appointmentId={}，status={}", id, status);
            return true;
        }
        if ("已取消".equals(status) || "已完成".equals(status)) {
            throw new BizException(409, "预约单当前状态不允许抢单");
        }
        int updated = jdbcOperations.update(
            """
                UPDATE apt_appointment
                SET doctor_id = ?,
                    status = '已接单',
                    update_time = CURRENT_TIMESTAMP
                WHERE id = ? AND deleted = 0
                """,
            doctorId,
            id
        );
        return updated > 0;
    }

    /**
     * 锁定一个可用号源。
     *
     * @param scheduleId 排班编号
     * @return 锁定后的号源
     */
    @Transactional
    public NumberSource lockNumberSource(Long scheduleId) {
        log.info("锁定号源，scheduleId={}", scheduleId);
        List<Map<String, Object>> rows = jdbcOperations.queryForList(
            """
                SELECT id, schedule_id, number_seq, status
                FROM apt_number_source
                WHERE schedule_id = ? AND status = 'AVAILABLE' AND deleted = 0
                ORDER BY number_seq, id
                LIMIT 1
                """,
            scheduleId
        );
        if (rows.isEmpty()) {
            throw new BizException(404, "暂无可用号源");
        }
        Map<String, Object> row = rows.get(0);
        Long id = ((Number) row.get("id")).longValue();
        int updated = jdbcOperations.update(
            """
                UPDATE apt_number_source
                SET status = 'LOCKED',
                    lock_time = CURRENT_TIMESTAMP,
                    update_time = CURRENT_TIMESTAMP
                WHERE id = ? AND status = 'AVAILABLE' AND deleted = 0
                """,
            id
        );
        if (updated == 0) {
            throw new BizException(409, "号源已被锁定");
        }
        return new NumberSource(
            id,
            ((Number) row.get("schedule_id")).longValue(),
            ((Number) row.get("number_seq")).intValue(),
            NumberSourceStatus.LOCKED
        );
    }

    /**
     * 创建放号配置。
     *
     * @param command 放号配置命令
     * @return 创建后的配置
     */
    @Transactional
    public Map<String, Object> createReleaseConfig(Map<String, Object> command) {
        Long scheduleId = requiredLong(command, "scheduleId", "排班编号不能为空");
        String releaseAt = requiredString(command, "releaseAt", "放号时间不能为空");
        int releaseCount = intValue(command, "releaseCount", 10);
        String status = stringValue(command, "status", "启用");
        log.info("创建放号配置，scheduleId={}，releaseAt={}，releaseCount={}", scheduleId, releaseAt, releaseCount);
        assertScheduleExists(scheduleId);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator creator = connection -> {
            PreparedStatement statement = connection.prepareStatement(
                """
                    INSERT INTO apt_number_source_release_config (tenant_id, schedule_id, release_time, release_count, status)
                    VALUES (?, ?, CAST(? AS TIMESTAMP), ?, ?)
                    """,
                new String[]{"id"}
            );
            statement.setLong(1, DEFAULT_TENANT_ID);
            statement.setLong(2, scheduleId);
            statement.setString(3, releaseAt);
            statement.setInt(4, releaseCount);
            statement.setString(5, status);
            return statement;
        };
        jdbcOperations.update(creator, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new BizException(500, "放号配置创建后未返回主键");
        }
        return row(
            "id", key.longValue(),
            "scheduleId", scheduleId,
            "releaseAt", releaseAt,
            "releaseCount", releaseCount,
            "status", status
        );
    }

    /**
     * 锁定并返回一个可用号源编号。
     *
     * @param scheduleId 排班编号
     * @return 号源编号
     */
    private Long lockAvailableNumberSource(Long scheduleId) {
        NumberSource numberSource = lockNumberSource(scheduleId);
        return numberSource.id();
    }

    /**
     * 查询预约单当前状态。
     *
     * @param id 预约单编号
     * @return 当前状态
     */
    private String queryAppointmentStatus(Long id) {
        List<String> rows = jdbcOperations.query(
            "SELECT status FROM apt_appointment WHERE id = ? AND deleted = 0",
            (resultSet, rowNum) -> resultSet.getString("status"),
            id
        );
        if (rows.isEmpty()) {
            throw new BizException(404, "预约单不存在");
        }
        return rows.get(0);
    }

    private void assertScheduleExists(Long scheduleId) {
        Integer count = jdbcOperations.queryForObject(
            "SELECT COUNT(1) FROM apt_number_source WHERE schedule_id = ? AND deleted = 0",
            Integer.class,
            scheduleId
        );
        if (count == null || count == 0) {
            throw new BizException(404, "排班号源不存在");
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
            throw new BizException(400, key + "必须是数字");
        }
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
        for (int index = 0; index < values.length; index += 2) {
            row.put(Objects.toString(values[index]), values[index + 1]);
        }
        return row;
    }
}

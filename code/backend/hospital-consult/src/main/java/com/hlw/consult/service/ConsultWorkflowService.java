package com.hlw.consult.service;

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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 问诊工作流服务，负责问诊创建、接单、完成和延长状态落库。
 */
@Service
public class ConsultWorkflowService {
    private static final Logger log = LoggerFactory.getLogger(ConsultWorkflowService.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final long DEFAULT_TENANT_ID = 100L;
    private static final long DEFAULT_PATIENT_ID = 1L;
    private static final long DEFAULT_DOCTOR_ID = 1L;
    private static final int DEFAULT_DURATION_LIMIT = 30;
    private static final int EXTEND_MINUTES = 15;

    private final JdbcOperations jdbcOperations;

    /**
     * 构造问诊工作流服务。
     *
     * @param jdbcOperations JDBC 操作组件
     */
    public ConsultWorkflowService(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    /**
     * 创建问诊单并记录首条主诉消息。
     *
     * @param command 问诊创建命令
     * @return 创建后的问诊单
     */
    @Transactional
    public Map<String, Object> createConsult(Map<String, Object> command) {
        Long patientId = longValue(command, "patientId", DEFAULT_PATIENT_ID);
        Long doctorId = longValue(command, "doctorId", DEFAULT_DOCTOR_ID);
        String consultType = stringValue(command, "type", "IMAGE_TEXT");
        String patientName = stringValue(command, "patientName", "赵晓岚");
        String doctorName = stringValue(command, "doctorName", "陈知衡");
        String channel = stringValue(command, "channel", channelName(consultType));
        String chiefComplaint = stringValue(command, "chiefComplaint", "");
        BigDecimal feeAmount = decimalValue(command, "feeAmount", new BigDecimal("39.90"));
        log.info("创建问诊单，patientId={}，doctorId={}，consultType={}", patientId, doctorId, consultType);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator creator = connection -> {
            PreparedStatement statement = connection.prepareStatement(
                """
                    INSERT INTO con_consult (
                        tenant_id, patient_id, doctor_id, consult_type, consult_no, patient_name,
                        doctor_name, channel, status, fee_amount, duration_limit, remaining_seconds, updated_at
                    )
                    VALUES (?, ?, ?, ?, '', ?, ?, ?, '待接单', ?, ?, ?, ?)
                    """,
                new String[]{"id"}
            );
            statement.setLong(1, DEFAULT_TENANT_ID);
            statement.setLong(2, patientId);
            statement.setLong(3, doctorId);
            statement.setString(4, consultType);
            statement.setString(5, patientName);
            statement.setString(6, doctorName);
            statement.setString(7, channel);
            statement.setBigDecimal(8, feeAmount);
            statement.setInt(9, DEFAULT_DURATION_LIMIT);
            statement.setInt(10, DEFAULT_DURATION_LIMIT * 60);
            statement.setString(11, currentDisplayTime());
            return statement;
        };
        jdbcOperations.update(creator, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new BizException(500, "问诊单创建后未返回主键");
        }
        long consultId = key.longValue();
        String consultNo = "ZX20260613" + String.format("%04d", consultId);
        jdbcOperations.update(
            "UPDATE con_consult SET consult_no = ?, update_time = CURRENT_TIMESTAMP WHERE id = ?",
            consultNo,
            consultId
        );
        insertChiefComplaintMessage(consultId, patientId, chiefComplaint);
        return row(
            "key", String.valueOf(consultId),
            "id", consultId,
            "consultNo", consultNo,
            "patientName", patientName,
            "doctorName", doctorName,
            "channel", channel,
            "status", "待接单",
            "updatedAt", currentDisplayTime()
        );
    }

    /**
     * 医生接单问诊。
     *
     * @param id 问诊编号
     * @param command 接单命令
     * @return 接单后的问诊单
     */
    @Transactional
    public Map<String, Object> accept(Long id, Map<String, Long> command) {
        Long doctorId = command == null ? null : command.get("doctorId");
        log.info("医生接单问诊，consultId={}，doctorId={}", id, doctorId);
        Map<String, Object> consult = queryConsult(id);
        String status = Objects.toString(consult.get("status"));
        if ("咨询中".equals(status) || "已延长".equals(status)) {
            log.info("问诊单无需重复接单，consultId={}，status={}", id, status);
            return row("key", String.valueOf(id), "id", id, "status", status);
        }
        if (!"待接单".equals(status)) {
            throw new BizException(409, "问诊单当前状态不允许接单");
        }
        if (doctorId == null) {
            doctorId = ((Number) consult.get("doctor_id")).longValue();
        }
        jdbcOperations.update(
            """
                UPDATE con_consult
                SET doctor_id = ?,
                    status = '咨询中',
                    start_time = CURRENT_TIMESTAMP,
                    duration_limit = CASE WHEN duration_limit <= 0 THEN ? ELSE duration_limit END,
                    remaining_seconds = CASE WHEN remaining_seconds <= 0 THEN ? ELSE remaining_seconds END,
                    updated_at = ?,
                    update_time = CURRENT_TIMESTAMP
                WHERE id = ? AND deleted = 0
                """,
            doctorId,
            DEFAULT_DURATION_LIMIT,
            DEFAULT_DURATION_LIMIT * 60,
            currentDisplayTime(),
            id
        );
        return row("key", String.valueOf(id), "id", id, "status", "咨询中");
    }

    /**
     * 完成问诊。
     *
     * @param id 问诊编号
     * @return 完成后的问诊单
     */
    @Transactional
    public Map<String, Object> complete(Long id) {
        log.info("完成问诊，consultId={}", id);
        String status = Objects.toString(queryConsult(id).get("status"));
        if ("已完成".equals(status)) {
            log.info("问诊单无需重复完成，consultId={}", id);
            return row("key", String.valueOf(id), "id", id, "status", status);
        }
        if ("已取消".equals(status) || "已超时".equals(status)) {
            throw new BizException(409, "问诊单当前状态不允许完成");
        }
        jdbcOperations.update(
            """
                UPDATE con_consult
                SET status = '已完成',
                    remaining_seconds = 0,
                    end_time = CURRENT_TIMESTAMP,
                    updated_at = ?,
                    update_time = CURRENT_TIMESTAMP
                WHERE id = ? AND deleted = 0
                """,
            currentDisplayTime(),
            id
        );
        return row("key", String.valueOf(id), "id", id, "status", "已完成");
    }

    /**
     * 延长问诊服务时长。
     *
     * @param id 问诊编号
     * @return 延长后的问诊单
     */
    @Transactional
    public Map<String, Object> extend(Long id) {
        log.info("延长问诊，consultId={}，extendMinutes={}", id, EXTEND_MINUTES);
        String status = Objects.toString(queryConsult(id).get("status"));
        if (!"咨询中".equals(status) && !"已延长".equals(status)) {
            throw new BizException(409, "问诊单当前状态不允许延长");
        }
        jdbcOperations.update(
            """
                UPDATE con_consult
                SET status = '已延长',
                    duration_limit = duration_limit + ?,
                    remaining_seconds = remaining_seconds + ?,
                    updated_at = ?,
                    update_time = CURRENT_TIMESTAMP
                WHERE id = ? AND deleted = 0
                """,
            EXTEND_MINUTES,
            EXTEND_MINUTES * 60,
            currentDisplayTime(),
            id
        );
        return row("key", String.valueOf(id), "id", id, "status", "已延长", "extendMinutes", EXTEND_MINUTES);
    }

    /**
     * 查询问诊单。
     *
     * @param id 问诊编号
     * @return 问诊单数据库行
     */
    private Map<String, Object> queryConsult(Long id) {
        List<Map<String, Object>> rows = jdbcOperations.queryForList(
            "SELECT id, doctor_id, status FROM con_consult WHERE id = ? AND deleted = 0",
            id
        );
        if (rows.isEmpty()) {
            throw new BizException(404, "问诊单不存在");
        }
        return rows.get(0);
    }

    /**
     * 写入首条主诉消息。
     *
     * @param consultId 问诊编号
     * @param patientId 患者编号
     * @param chiefComplaint 主诉内容
     */
    private void insertChiefComplaintMessage(Long consultId, Long patientId, String chiefComplaint) {
        if (chiefComplaint.isBlank()) {
            return;
        }
        jdbcOperations.update(
            """
                INSERT INTO con_message (tenant_id, consult_id, sender_id, sender_type, content, content_type, read_flag)
                VALUES (?, ?, ?, 'PATIENT', ?, 'TEXT', FALSE)
                """,
            DEFAULT_TENANT_ID,
            consultId,
            patientId,
            chiefComplaint
        );
    }

    /**
     * 读取可选字符串字段。
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
     * 按问诊类型转换展示渠道。
     *
     * @param consultType 问诊类型
     * @return 展示渠道
     */
    private String channelName(String consultType) {
        if ("VIDEO".equalsIgnoreCase(consultType)) {
            return "视频";
        }
        return "图文";
    }

    /**
     * 获取当前展示时间。
     *
     * @return 时分展示值
     */
    private String currentDisplayTime() {
        return LocalTime.now().format(TIME_FORMATTER);
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

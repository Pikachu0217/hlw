package com.hlw.prescription.service;

import com.hlw.common.core.exception.BizException;
import com.hlw.common.mq.core.MqProducer;
import com.hlw.common.mq.model.MqMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 处方工作流服务，负责处方草稿、提审、审核和驳回落库。
 */
@Service
public class PrescriptionWorkflowService {
    private static final Logger log = LoggerFactory.getLogger(PrescriptionWorkflowService.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final long DEFAULT_TENANT_ID = 100L;
    private static final long DEFAULT_CONSULT_ID = 1L;
    private static final long DEFAULT_PATIENT_ID = 1L;
    private static final long DEFAULT_DOCTOR_ID = 1L;

    private final JdbcOperations jdbcOperations;
    private final MqProducer mqProducer;

    /**
     * 构造处方工作流服务。
     *
     * @param jdbcOperations JDBC 操作组件
     * @param mqProducer 消息生产者
     */
    public PrescriptionWorkflowService(JdbcOperations jdbcOperations, MqProducer mqProducer) {
        this.jdbcOperations = jdbcOperations;
        this.mqProducer = mqProducer;
    }

    /**
     * 创建处方草稿并写入药品明细。
     *
     * @param command 创建命令
     * @return 创建后的处方
     */
    @Transactional
    public Map<String, Object> create(Map<String, Object> command) {
        Long consultId = longValue(command, "consultId", DEFAULT_CONSULT_ID);
        Long patientId = longValue(command, "patientId", DEFAULT_PATIENT_ID);
        Long doctorId = longValue(command, "doctorId", DEFAULT_DOCTOR_ID);
        String patientName = stringValue(command, "patientName", "赵晓岚");
        String doctorName = stringValue(command, "doctorName", "陈知衡");
        List<Long> drugIds = longList(command.get("drugIds"));
        int drugCount = drugIds.isEmpty() ? intValue(command, "drugCount", 1) : drugIds.size();
        String issuedAt = stringValue(command, "issuedAt", currentDisplayTime());
        log.info("创建处方草稿，consultId={}，patientId={}，doctorId={}，drugCount={}", consultId, patientId, doctorId, drugCount);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator creator = connection -> {
            PreparedStatement statement = connection.prepareStatement(
                """
                    INSERT INTO pre_prescription (
                        tenant_id, consult_id, patient_id, doctor_id, prescription_no,
                        patient_name, doctor_name, drug_count, issued_at, status
                    )
                    VALUES (?, ?, ?, ?, '', ?, ?, ?, ?, '草稿')
                    """,
                new String[]{"id"}
            );
            statement.setLong(1, DEFAULT_TENANT_ID);
            statement.setLong(2, consultId);
            statement.setLong(3, patientId);
            statement.setLong(4, doctorId);
            statement.setString(5, patientName);
            statement.setString(6, doctorName);
            statement.setInt(7, drugCount);
            statement.setString(8, issuedAt);
            return statement;
        };
        jdbcOperations.update(creator, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new BizException(500, "处方创建后未返回主键");
        }
        long prescriptionId = key.longValue();
        String prescriptionNo = "CF20260613" + String.format("%04d", prescriptionId);
        jdbcOperations.update(
            "UPDATE pre_prescription SET prescription_no = ?, update_time = CURRENT_TIMESTAMP WHERE id = ?",
            prescriptionNo,
            prescriptionId
        );
        insertItems(prescriptionId, drugIds);
        return prescriptionRow(prescriptionId, prescriptionNo, patientName, doctorName, drugCount, issuedAt, "草稿");
    }

    /**
     * 提交处方进入待审方状态。
     *
     * @param id 处方编号
     * @return 提交后的处方
     */
    @Transactional
    public Map<String, Object> submit(Long id) {
        log.info("提交处方，prescriptionId={}", id);
        String status = queryStatus(id);
        if ("待审方".equals(status)) {
            log.info("处方无需重复提交，prescriptionId={}", id);
            return row("key", String.valueOf(id), "id", id, "status", status);
        }
        if (!"草稿".equals(status)) {
            throw new BizException(409, "处方当前状态不允许提交");
        }
        jdbcOperations.update(
            """
                UPDATE pre_prescription
                SET status = '待审方',
                    submit_time = CURRENT_TIMESTAMP,
                    update_time = CURRENT_TIMESTAMP
                WHERE id = ? AND deleted = 0
                """,
            id
        );
        return row("key", String.valueOf(id), "id", id, "status", "待审方");
    }

    /**
     * 审核通过处方。
     *
     * @param id 处方编号
     * @param pharmacistId 药师编号
     * @param remark 审核备注
     * @return 审核后的处方
     */
    @Transactional
    public Map<String, Object> approve(Long id, Long pharmacistId, String remark) {
        log.info("审核通过处方，prescriptionId={}，pharmacistId={}", id, pharmacistId);
        String status = queryStatus(id);
        if ("待发药".equals(status)) {
            log.info("处方无需重复审核通过，prescriptionId={}", id);
            return row("key", String.valueOf(id), "id", id, "status", status);
        }
        if (!"待审方".equals(status)) {
            throw new BizException(409, "处方当前状态不允许审核通过");
        }
        jdbcOperations.update(
            """
                UPDATE pre_prescription
                SET status = '待发药',
                    pharmacist_id = ?,
                    audit_remark = ?,
                    audit_time = CURRENT_TIMESTAMP,
                    update_time = CURRENT_TIMESTAMP
                WHERE id = ? AND deleted = 0
                """,
            pharmacistId,
            remark,
            id
        );
        mqProducer.publish(new MqMessage("prescription.audited", "{\"prescriptionId\":" + id + "}", 0, 0, 3));
        return row("key", String.valueOf(id), "id", id, "status", "待发药", "remark", remark);
    }

    /**
     * 驳回处方。
     *
     * @param id 处方编号
     * @param remark 驳回备注
     * @return 驳回后的处方
     */
    @Transactional
    public Map<String, Object> reject(Long id, String remark) {
        log.info("驳回处方，prescriptionId={}，remark={}", id, remark);
        String status = queryStatus(id);
        if ("已驳回".equals(status)) {
            log.info("处方无需重复驳回，prescriptionId={}", id);
            return row("key", String.valueOf(id), "id", id, "status", status, "remark", remark);
        }
        if (!"待审方".equals(status)) {
            throw new BizException(409, "处方当前状态不允许驳回");
        }
        jdbcOperations.update(
            """
                UPDATE pre_prescription
                SET status = '已驳回',
                    audit_remark = ?,
                    audit_time = CURRENT_TIMESTAMP,
                    update_time = CURRENT_TIMESTAMP
                WHERE id = ? AND deleted = 0
                """,
            remark,
            id
        );
        return row("key", String.valueOf(id), "id", id, "status", "已驳回", "remark", remark);
    }

    /**
     * 查询处方状态。
     *
     * @param id 处方编号
     * @return 当前状态
     */
    private String queryStatus(Long id) {
        List<String> rows = jdbcOperations.query(
            "SELECT status FROM pre_prescription WHERE id = ? AND deleted = 0",
            (resultSet, rowNum) -> resultSet.getString("status"),
            id
        );
        if (rows.isEmpty()) {
            throw new BizException(404, "处方不存在");
        }
        return rows.get(0);
    }

    /**
     * 写入处方药品明细。
     *
     * @param prescriptionId 处方编号
     * @param drugIds 药品编号列表
     */
    private void insertItems(Long prescriptionId, List<Long> drugIds) {
        if (drugIds.isEmpty()) {
            jdbcOperations.update(
                """
                    INSERT INTO pre_prescription_item (tenant_id, prescription_id, drug_id, drug_name, dosage, frequency, quantity, usage_note)
                    VALUES (?, ?, 1, '接口测试药品', '遵医嘱', '每日一次', 1, '饭后服用')
                    """,
                DEFAULT_TENANT_ID,
                prescriptionId
            );
            return;
        }
        for (Long drugId : drugIds) {
            jdbcOperations.update(
                """
                    INSERT INTO pre_prescription_item (tenant_id, prescription_id, drug_id, drug_name, dosage, frequency, quantity, usage_note)
                    VALUES (?, ?, ?, ?, '遵医嘱', '每日一次', 1, '饭后服用')
                    """,
                DEFAULT_TENANT_ID,
                prescriptionId,
                drugId,
                "药品" + drugId
            );
        }
    }

    /**
     * 构造处方返回行。
     *
     * @param id 处方编号
     * @param prescriptionNo 处方号
     * @param patientName 患者姓名
     * @param doctorName 医生姓名
     * @param drugCount 药品数量
     * @param issuedAt 开方时间
     * @param status 处方状态
     * @return 处方行
     */
    private Map<String, Object> prescriptionRow(
        Long id,
        String prescriptionNo,
        String patientName,
        String doctorName,
        int drugCount,
        String issuedAt,
        String status
    ) {
        return row(
            "key", String.valueOf(id),
            "id", id,
            "prescriptionNo", prescriptionNo,
            "patientName", patientName,
            "doctorName", doctorName,
            "drugCount", drugCount,
            "issuedAt", issuedAt,
            "status", status
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
     * 读取长整型列表。
     *
     * @param value 请求字段值
     * @return 长整型列表
     */
    private List<Long> longList(Object value) {
        if (!(value instanceof List<?> items)) {
            return List.of();
        }
        return items.stream()
            .filter(Objects::nonNull)
            .map(item -> Long.parseLong(Objects.toString(item)))
            .toList();
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

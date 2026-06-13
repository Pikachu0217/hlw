package com.hlw.patient.service;

import com.hlw.common.core.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * JDBC 患者仓储，负责将患者资料读写到 pat_patient 表。
 */
public class JdbcPatientRepository implements PatientRepository {
    private static final Logger log = LoggerFactory.getLogger(JdbcPatientRepository.class);

    private final JdbcOperations jdbcOperations;

    /**
     * 构造 JDBC 患者仓储。
     *
     * @param jdbcOperations JDBC 操作组件
     */
    public JdbcPatientRepository(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    /**
     * 保存患者档案。
     *
     * @param patientId 患者编号
     * @param command 更新命令
     * @param maskedPhone 脱敏手机号
     * @return 患者档案
     */
    @Override
    @Transactional
    public PatientProfile save(Long patientId, UpdatePatientProfileCommand command, String maskedPhone) {
        requireText(command.name(), "患者姓名不能为空");
        requireText(command.phone(), "联系电话不能为空");
        requireText(command.gender(), "患者性别不能为空");
        log.info("写入患者档案，patientId={}，name={}", patientId, command.name());
        int updated = jdbcOperations.update("""
            UPDATE pat_patient
            SET name = ?, patient_name = ?, phone = ?, gender = ?, update_time = CURRENT_TIMESTAMP
            WHERE id = ? AND deleted = 0
            """, command.name(), command.name(), command.phone(), command.gender(), patientId);
        if (updated == 0) {
            throw new BizException(404, "患者档案不存在");
        }
        return new PatientProfile(patientId, command.name(), maskedPhone, command.gender());
    }

    /**
     * 按患者编号查询档案。
     *
     * @param patientId 患者编号
     * @return 患者档案
     */
    @Override
    public PatientProfile findById(Long patientId) {
        log.info("读取患者档案，patientId={}", patientId);
        List<Map<String, Object>> rows = jdbcOperations.queryForList("""
            SELECT id,
                   patient_name AS name,
                   phone,
                   gender
            FROM pat_patient
            WHERE id = ? AND deleted = 0
            ORDER BY id
            LIMIT 1
            """, patientId);
        if (rows.isEmpty()) {
            throw new BizException(404, "患者档案不存在");
        }
        Map<String, Object> row = rows.get(0);
        return new PatientProfile(
            ((Number) row.get("id")).longValue(),
            String.valueOf(row.get("name")),
            maskPhone(String.valueOf(row.get("phone"))),
            String.valueOf(row.get("gender"))
        );
    }

    /**
     * 校验文本非空。
     *
     * @param value 字段值
     * @param message 错误消息
     */
    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BizException(400, message);
        }
    }

    /**
     * 手机号脱敏。
     *
     * @param phone 手机号
     * @return 脱敏手机号
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}

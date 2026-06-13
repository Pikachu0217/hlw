package com.hlw.order.service;

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

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 订单工作流服务，负责订单创建、支付状态变更和支付事件发布。
 */
@Service
public class OrderWorkflowService {
    private static final Logger log = LoggerFactory.getLogger(OrderWorkflowService.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final long DEFAULT_TENANT_ID = 100L;
    private static final long DEFAULT_PATIENT_ID = 1L;

    private final JdbcOperations jdbcOperations;
    private final MqProducer mqProducer;

    /**
     * 构造订单工作流服务。
     *
     * @param jdbcOperations JDBC 操作组件
     * @param mqProducer 消息生产者
     */
    public OrderWorkflowService(JdbcOperations jdbcOperations, MqProducer mqProducer) {
        this.jdbcOperations = jdbcOperations;
        this.mqProducer = mqProducer;
    }

    /**
     * 创建待支付订单。
     *
     * @param command 订单创建命令
     * @return 创建后的订单
     */
    @Transactional
    public Map<String, Object> create(Map<String, Object> command) {
        String bizType = stringValue(command, "bizType", stringValue(command, "businessType", "APPOINTMENT"));
        String businessType = businessTypeName(bizType);
        Long bizId = longValue(command, "bizId", 0L);
        Long patientId = longValue(command, "patientId", DEFAULT_PATIENT_ID);
        String patientName = stringValue(command, "patientName", "张小满");
        BigDecimal amount = decimalValue(command, "amount", new BigDecimal("25.00"));
        String createdAt = stringValue(command, "createdAt", currentDisplayTime());
        log.info("创建订单，bizType={}，bizId={}，patientId={}，amount={}", bizType, bizId, patientId, amount);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator creator = connection -> {
            PreparedStatement statement = connection.prepareStatement(
                """
                    INSERT INTO ord_order (
                        tenant_id, order_no, biz_type, biz_id, patient_id, business_type,
                        patient_name, amount, pay_status, created_at, status
                    )
                    VALUES (?, '', ?, ?, ?, ?, ?, ?, '待支付', ?, '待支付')
                    """,
                new String[]{"id"}
            );
            statement.setLong(1, DEFAULT_TENANT_ID);
            statement.setString(2, bizType);
            statement.setLong(3, bizId);
            statement.setLong(4, patientId);
            statement.setString(5, businessType);
            statement.setString(6, patientName);
            statement.setBigDecimal(7, amount);
            statement.setString(8, createdAt);
            return statement;
        };
        jdbcOperations.update(creator, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new BizException(500, "订单创建后未返回主键");
        }
        long orderId = key.longValue();
        String orderNo = "DD20260613" + String.format("%04d", orderId);
        jdbcOperations.update(
            "UPDATE ord_order SET order_no = ?, update_time = CURRENT_TIMESTAMP WHERE id = ?",
            orderNo,
            orderId
        );
        return orderRow(orderId, orderNo, businessType, patientName, amount, "待支付", createdAt);
    }

    /**
     * 支付订单。
     *
     * @param id 订单编号
     * @param payMethod 支付方式
     * @return 支付后的订单
     */
    @Transactional
    public Map<String, Object> pay(Long id, String payMethod) {
        log.info("订单支付，orderId={}，payMethod={}", id, payMethod);
        Map<String, Object> order = queryOrder(id);
        String payStatus = Objects.toString(order.get("pay_status"));
        if ("已支付".equals(payStatus)) {
            log.info("订单无需重复支付，orderId={}", id);
            return orderRow(order, "已支付");
        }
        if (!"待支付".equals(payStatus)) {
            throw new BizException(409, "订单当前状态不允许支付");
        }
        jdbcOperations.update(
            """
                UPDATE ord_order
                SET pay_status = '已支付',
                    status = '已支付',
                    pay_method = ?,
                    pay_time = CURRENT_TIMESTAMP,
                    update_time = CURRENT_TIMESTAMP
                WHERE id = ? AND deleted = 0
                """,
            payMethod,
            id
        );
        mqProducer.publish(new MqMessage("order.paid", "{\"orderId\":" + id + "}", 0, 0, 3));
        return orderRow(order, "已支付");
    }

    /**
     * 查询订单。
     *
     * @param id 订单编号
     * @return 订单数据库行
     */
    private Map<String, Object> queryOrder(Long id) {
        List<Map<String, Object>> rows = jdbcOperations.queryForList(
            """
                SELECT id, order_no, business_type, patient_name, amount, pay_status, created_at
                FROM ord_order
                WHERE id = ? AND deleted = 0
                """,
            id
        );
        if (rows.isEmpty()) {
            throw new BizException(404, "订单不存在");
        }
        return rows.get(0);
    }

    /**
     * 构造订单返回行。
     *
     * @param row 数据库行
     * @param payStatus 支付状态
     * @return 订单返回行
     */
    private Map<String, Object> orderRow(Map<String, Object> row, String payStatus) {
        return orderRow(
            ((Number) row.get("id")).longValue(),
            Objects.toString(row.get("order_no")),
            Objects.toString(row.get("business_type")),
            Objects.toString(row.get("patient_name")),
            (BigDecimal) row.get("amount"),
            payStatus,
            Objects.toString(row.get("created_at"))
        );
    }

    /**
     * 构造订单返回行。
     *
     * @param id 订单编号
     * @param orderNo 订单号
     * @param businessType 业务类型
     * @param patientName 患者姓名
     * @param amount 订单金额
     * @param payStatus 支付状态
     * @param createdAt 创建展示时间
     * @return 订单返回行
     */
    private Map<String, Object> orderRow(
        Long id,
        String orderNo,
        String businessType,
        String patientName,
        BigDecimal amount,
        String payStatus,
        String createdAt
    ) {
        return row(
            "key", String.valueOf(id),
            "id", id,
            "orderNo", orderNo,
            "businessType", businessType,
            "patientName", patientName,
            "amount", "¥" + amount.setScale(2),
            "payStatus", payStatus,
            "createdAt", createdAt
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
     * 转换业务类型展示名称。
     *
     * @param bizType 业务类型编码
     * @return 展示名称
     */
    private String businessTypeName(String bizType) {
        return switch (bizType.toUpperCase()) {
            case "CONSULT" -> "图文咨询";
            case "PRESCRIPTION" -> "处方购药";
            case "DRUG" -> "药品配送";
            default -> "门诊预约";
        };
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

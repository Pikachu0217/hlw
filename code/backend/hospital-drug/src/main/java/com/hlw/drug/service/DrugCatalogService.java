package com.hlw.drug.service;

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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 药品目录和库存服务，负责药品资料、库存记录和配送发货落库。
 */
@Service
public class DrugCatalogService {
    private static final Logger log = LoggerFactory.getLogger(DrugCatalogService.class);
    private static final long DEFAULT_TENANT_ID = 100L;

    private final JdbcOperations jdbcOperations;
    private final MqProducer mqProducer;

    /**
     * 构造药品目录和库存服务。
     *
     * @param jdbcOperations JDBC 操作组件
     * @param mqProducer 消息生产者
     */
    public DrugCatalogService(JdbcOperations jdbcOperations, MqProducer mqProducer) {
        this.jdbcOperations = jdbcOperations;
        this.mqProducer = mqProducer;
    }

    /**
     * 创建药品资料。
     *
     * @param command 药品创建命令
     * @return 创建后的药品资料
     */
    @Transactional
    public Map<String, Object> createDrug(Map<String, Object> command) {
        String drugName = requiredString(command, "drugName", "药品名称不能为空");
        String spec = requiredString(command, "spec", "药品规格不能为空");
        int inventory = intValue(command, "inventory", 0);
        String unit = stringValue(command, "unit", "盒");
        String warningStatus = warningStatus(inventory);
        log.info("创建药品资料，drugName={}，inventory={}", drugName, inventory);
        Long id = insertDrugInfo(drugName, spec, inventory, unit, warningStatus);
        if (inventory > 0) {
            insertDrugStock(id, "中心药房", inventory, warningStatus);
        }
        return row(
            "key", String.valueOf(id),
            "drugName", drugName,
            "spec", spec,
            "inventory", inventory,
            "unit", unit,
            "warningStatus", warningStatus
        );
    }

    /**
     * 创建库存记录。
     *
     * @param command 库存创建命令
     * @return 创建后的库存记录
     */
    @Transactional
    public Map<String, Object> createStock(Map<String, Object> command) {
        Long drugId = requiredLong(command, "drugId", "药品编号不能为空");
        String warehouseName = requiredString(command, "warehouseName", "仓库名称不能为空");
        int inventory = intValue(command, "inventory", 0);
        String warningStatus = warningStatus(inventory);
        log.info("创建库存记录，drugId={}，warehouseName={}，inventory={}", drugId, warehouseName, inventory);
        String drugName = queryDrugName(drugId);
        Long id = insertDrugStock(drugId, warehouseName, inventory, warningStatus);
        refreshDrugInventory(drugId);
        return row(
            "key", String.valueOf(id),
            "drugName", drugName,
            "warehouseName", warehouseName,
            "inventory", inventory,
            "warningStatus", warningStatus
        );
    }

    /**
     * 将配送单更新为已发货并发送发货事件。
     *
     * @param deliveryId 配送单编号
     * @return 发货结果
     */
    @Transactional
    public Map<String, Object> shipDelivery(Long deliveryId) {
        log.info("药品配送发货，deliveryId={}", deliveryId);
        int updated = jdbcOperations.update("""
            UPDATE drug_delivery
            SET status = 'SHIPPED',
                ship_time = CURRENT_TIMESTAMP,
                update_time = CURRENT_TIMESTAMP
            WHERE id = ? AND status = 'PENDING' AND deleted = 0
            """, deliveryId);
        if (updated == 0) {
            assertDeliveryCanShip(deliveryId);
            log.info("药品配送单已发货，跳过重复事件，deliveryId={}", deliveryId);
            return row("id", deliveryId, "status", "SHIPPED");
        }
        mqProducer.publish(new MqMessage("drug.shipped", "{\"deliveryId\":" + deliveryId + "}", 0, 0, 3));
        log.info("药品配送事件已发布，deliveryId={}", deliveryId);
        return row("id", deliveryId, "status", "SHIPPED");
    }

    /**
     * 插入药品主表并返回数据库生成的主键。
     *
     * @param drugName 药品名称
     * @param spec 药品规格
     * @param inventory 初始库存
     * @param unit 库存单位
     * @param warningStatus 预警状态
     * @return 药品编号
     */
    private Long insertDrugInfo(String drugName, String spec, int inventory, String unit, String warningStatus) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator creator = connection -> {
            var statement = connection.prepareStatement("""
                INSERT INTO drug_info (tenant_id, name, drug_name, spec, inventory, unit, warning_status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, new String[] {"id"});
            statement.setLong(1, DEFAULT_TENANT_ID);
            statement.setString(2, drugName);
            statement.setString(3, drugName);
            statement.setString(4, spec);
            statement.setInt(5, inventory);
            statement.setString(6, unit);
            statement.setString(7, warningStatus);
            return statement;
        };
        jdbcOperations.update(creator, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new BizException(500, "药品主键生成失败");
        }
        return key.longValue();
    }

    /**
     * 插入库存记录并返回数据库生成的主键。
     *
     * @param drugId 药品编号
     * @param warehouseName 仓库名称
     * @param inventory 库存数量
     * @param warningStatus 预警状态
     * @return 库存编号
     */
    private Long insertDrugStock(Long drugId, String warehouseName, int inventory, String warningStatus) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator creator = connection -> {
            var statement = connection.prepareStatement("""
                INSERT INTO drug_stock (tenant_id, drug_id, warehouse_name, inventory, warning_status, stock_qty)
                VALUES (?, ?, ?, ?, ?, ?)
                """, new String[] {"id"});
            statement.setLong(1, DEFAULT_TENANT_ID);
            statement.setLong(2, drugId);
            statement.setString(3, warehouseName);
            statement.setInt(4, inventory);
            statement.setString(5, warningStatus);
            statement.setBigDecimal(6, java.math.BigDecimal.valueOf(inventory));
            return statement;
        };
        jdbcOperations.update(creator, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new BizException(500, "库存主键生成失败");
        }
        return key.longValue();
    }

    /**
     * 校验配送单当前状态是否允许重复发货。
     *
     * @param deliveryId 配送单编号
     */
    private void assertDeliveryCanShip(Long deliveryId) {
        String status = jdbcOperations.query("""
            SELECT status
            FROM drug_delivery
            WHERE id = ? AND deleted = 0
            """, rs -> rs.next() ? rs.getString("status") : null, deliveryId);
        if (status == null) {
            throw new BizException(404, "配送单不存在");
        }
        if (!"SHIPPED".equals(status)) {
            throw new BizException(400, "当前配送状态不允许发货");
        }
    }

    /**
     * 查询药品名称并校验药品存在。
     *
     * @param drugId 药品编号
     * @return 药品名称
     */
    private String queryDrugName(Long drugId) {
        return jdbcOperations.query("""
            SELECT drug_name
            FROM drug_info
            WHERE id = ? AND deleted = 0
            """, rs -> {
            if (!rs.next()) {
                throw new BizException(404, "药品不存在");
            }
            return rs.getString("drug_name");
        }, drugId);
    }

    /**
     * 刷新药品主表库存冗余字段。
     *
     * @param drugId 药品编号
     */
    private void refreshDrugInventory(Long drugId) {
        jdbcOperations.update("""
            UPDATE drug_info
            SET inventory = (
                    SELECT COALESCE(SUM(inventory), 0)
                    FROM drug_stock
                    WHERE drug_id = ? AND deleted = 0
                ),
                warning_status = CASE
                    WHEN (
                        SELECT COALESCE(SUM(inventory), 0)
                        FROM drug_stock
                        WHERE drug_id = ? AND deleted = 0
                    ) < 50 THEN '预警'
                    ELSE '正常'
                END,
                update_time = CURRENT_TIMESTAMP
            WHERE id = ? AND deleted = 0
            """, drugId, drugId, drugId);
    }

    /**
     * 根据库存数量计算预警状态。
     *
     * @param inventory 库存数量
     * @return 预警状态
     */
    private String warningStatus(int inventory) {
        return inventory < 50 ? "预警" : "正常";
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

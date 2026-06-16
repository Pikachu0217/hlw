package com.hlw.common.mq.core;

import com.hlw.common.mq.model.MqMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;

import java.util.List;

/**
 * JDBC 本地消息存储。
 */
public class JdbcMqLocalMessageStore implements MqLocalMessageStore {
    private static final Logger log = LoggerFactory.getLogger(JdbcMqLocalMessageStore.class);
    private static final long DEFAULT_TENANT_ID = 100L;

    // JDBC 操作组件。
    private final JdbcOperations jdbcOperations;
    // local_message 表是否存在 tenant_id 字段。
    private final boolean tenantIdColumnExists;
    // local_message 表是否存在 payload 字段。
    private final boolean payloadColumnExists;
    // local_message 表是否存在 max_retry 字段。
    private final boolean maxRetryColumnExists;
    // local_message 表是否存在 deleted 字段。
    private final boolean deletedColumnExists;

    /**
     * 构造 JDBC 本地消息存储。
     *
     * @param jdbcOperations JDBC 操作组件
     */
    public JdbcMqLocalMessageStore(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
        this.tenantIdColumnExists = columnExists("tenant_id");
        this.payloadColumnExists = columnExists("payload");
        this.maxRetryColumnExists = columnExists("max_retry");
        this.deletedColumnExists = columnExists("deleted");
    }

    /**
     * 保存待发送消息。
     *
     * @param message 消息对象
     */
    @Override
    public void save(MqMessage message) {
        log.info("保存本地消息，topic={}", message.getTopic());
        List<String> columns = new java.util.ArrayList<>();
        List<Object> values = new java.util.ArrayList<>();
        if (tenantIdColumnExists) {
            columns.add("tenant_id");
            values.add(DEFAULT_TENANT_ID);
        }
        columns.add("topic");
        values.add(message.getTopic());
        if (payloadColumnExists) {
            columns.add("payload");
            values.add(message.getBody());
        }
        columns.add("body");
        values.add(message.getBody());
        columns.add("retry_count");
        values.add(message.getRetryCount());
        if (maxRetryColumnExists) {
            columns.add("max_retry");
            values.add(message.getMaxRetry());
        }
        columns.add("status");
        values.add("PENDING");

        String placeholders = String.join(", ", java.util.Collections.nCopies(columns.size(), "?"));
        jdbcOperations.update("""
            INSERT INTO local_message (%s)
            VALUES (%s)
            """.formatted(String.join(", ", columns), placeholders), values.toArray());
    }

    /**
     * 查询待发送消息。
     *
     * @param limit 查询数量限制
     * @return 待发送消息列表
     */
    @Override
    public List<MqMessage> findPending(int limit) {
        log.info("查询待发送本地消息，limit={}", limit);
        String bodyExpression = payloadColumnExists ? "COALESCE(NULLIF(payload, ''), body)" : "body";
        String maxRetryExpression = maxRetryColumnExists ? "max_retry" : "3";
        String deletedFilter = deletedColumnExists ? " AND deleted = 0" : "";
        return jdbcOperations.query("""
            SELECT topic, %s AS body, retry_count, %s AS max_retry
            FROM local_message
            WHERE status = 'PENDING'%s
            ORDER BY id
            LIMIT ?
            """.formatted(bodyExpression, maxRetryExpression, deletedFilter), (resultSet, rowNum) -> new MqMessage(
            resultSet.getString("topic"),
            resultSet.getString("body"),
            resultSet.getInt("retry_count"),
            resultSet.getInt("max_retry")
        ), limit);
    }

    /**
     * 标记消息已发送。
     *
     * @param message 消息对象
     */
    @Override
    public void markSent(MqMessage message) {
        log.info("标记本地消息已发送，topic={}", message.getTopic());
        String bodyExpression = payloadColumnExists ? "COALESCE(NULLIF(payload, ''), body)" : "body";
        String deletedFilter = deletedColumnExists ? " AND deleted = 0" : "";
        jdbcOperations.update("""
            UPDATE local_message
            SET status = 'SENT', update_time = CURRENT_TIMESTAMP
            WHERE id = (
                SELECT id FROM local_message
                WHERE status = 'PENDING'%s AND topic = ? AND %s = ?
                ORDER BY id
                LIMIT 1
            )
            """.formatted(deletedFilter, bodyExpression), message.getTopic(), message.getBody());
    }

    /**
     * 标记消息发送失败。
     *
     * @param message 消息对象
     * @param errorMessage 错误信息
     */
    @Override
    public void markFailed(MqMessage message, String errorMessage) {
        log.warn("标记本地消息发送失败，topic={}，error={}", message.getTopic(), errorMessage);
        String bodyExpression = payloadColumnExists ? "COALESCE(NULLIF(payload, ''), body)" : "body";
        String deletedFilter = deletedColumnExists ? " AND deleted = 0" : "";
        jdbcOperations.update("""
            UPDATE local_message
            SET status = 'FAILED', retry_count = ?, update_time = CURRENT_TIMESTAMP
            WHERE id = (
                SELECT id FROM local_message
                WHERE status = 'PENDING'%s AND topic = ? AND %s = ?
                ORDER BY id
                LIMIT 1
            )
            """.formatted(deletedFilter, bodyExpression), message.getRetryCount() + 1, message.getTopic(), message.getBody());
    }

    /**
     * 判断本地消息表是否存在指定字段，兼容不同初始化脚本的字段差异。
     *
     * @param columnName 字段名
     * @return 是否存在
     */
    private boolean columnExists(String columnName) {
        Integer count = jdbcOperations.queryForObject("""
            SELECT COUNT(1)
            FROM information_schema.columns
            WHERE table_schema = current_schema() AND table_name = 'local_message' AND column_name = ?
            """, Integer.class, columnName);
        return count != null && count > 0;
    }
}

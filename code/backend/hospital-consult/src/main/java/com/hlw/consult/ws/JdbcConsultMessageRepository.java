package com.hlw.consult.ws;

import com.hlw.common.core.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * JDBC 问诊消息仓储，负责将 WebSocket 消息读写到 con_message 表。
 */
public class JdbcConsultMessageRepository implements ConsultMessageRepository {
    private static final Logger log = LoggerFactory.getLogger(JdbcConsultMessageRepository.class);
    private static final long DEFAULT_TENANT_ID = 100L;

    private final JdbcOperations jdbcOperations;

    /**
     * 构造 JDBC 问诊消息仓储。
     *
     * @param jdbcOperations JDBC 操作组件
     */
    public JdbcConsultMessageRepository(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    /**
     * 保存问诊消息。
     *
     * @param message 问诊消息
     */
    @Override
    @Transactional
    public void save(ConsultMessage message) {
        if (message.content() == null || message.content().isBlank()) {
            throw new BizException(400, "消息内容不能为空");
        }
        String contentType = message.contentType() == null || message.contentType().isBlank() ? "TEXT" : message.contentType();
        log.info("保存问诊消息，consultId={}，senderId={}", message.consultId(), message.senderId());
        jdbcOperations.update("""
            INSERT INTO con_message (
                tenant_id, consult_id, sender_id, sender_type, content, content_type, read_flag, create_time
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """, DEFAULT_TENANT_ID, message.consultId(), message.senderId(), message.senderType(),
            message.content(), contentType, message.read(), message.createTime());
    }

    /**
     * 查询问诊消息列表。
     *
     * @param consultId 问诊编号
     * @return 问诊消息列表
     */
    @Override
    public List<ConsultMessage> findByConsultId(Long consultId) {
        log.info("读取问诊消息，consultId={}", consultId);
        return jdbcOperations.query("""
            SELECT consult_id,
                   sender_id,
                   sender_type,
                   content,
                   content_type,
                   read_flag,
                   create_time
            FROM con_message
            WHERE deleted = 0 AND consult_id = ?
            ORDER BY id
            """, (resultSet, rowNum) -> new ConsultMessage(
                resultSet.getLong("consult_id"),
                resultSet.getLong("sender_id"),
                resultSet.getString("sender_type"),
                resultSet.getString("content"),
                resultSet.getString("content_type"),
                resultSet.getBoolean("read_flag"),
                toLocalDateTime(resultSet.getObject("create_time"))
            ), consultId);
    }

    /**
     * 转换数据库时间字段。
     *
     * @param value 数据库时间值
     * @return 本地日期时间
     */
    private LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return null;
    }
}

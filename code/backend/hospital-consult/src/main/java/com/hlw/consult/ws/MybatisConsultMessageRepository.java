package com.hlw.consult.ws;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hlw.common.core.exception.BizException;
import com.hlw.consult.entity.ConMessageEntity;
import com.hlw.consult.mapper.ConMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * MyBatis Plus 问诊消息仓储，负责将 WebSocket 消息读写到 con_message 表。
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class MybatisConsultMessageRepository implements ConsultMessageRepository {
    /** 问诊消息数据访问组件。 */
    private final ConMessageMapper conMessageMapper;

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
        ConMessageEntity entity = new ConMessageEntity();
        entity.setConsultId(message.consultId());
        entity.setSenderId(message.senderId());
        entity.setSenderType(message.senderType());
        entity.setContent(message.content());
        entity.setContentType(contentType);
        entity.setReadFlag(message.read());
        entity.setIsRead(message.read() ? 1 : 0);
        entity.setCreateTime(message.createTime());
        entity.setDeleted(0);
        conMessageMapper.insert(entity);
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
        return conMessageMapper.selectList(new LambdaQueryWrapper<ConMessageEntity>()
                .eq(ConMessageEntity::getDeleted, 0)
                .eq(ConMessageEntity::getConsultId, consultId)
                .orderByAsc(ConMessageEntity::getId))
            .stream()
            .map(entity -> new ConsultMessage(
                entity.getConsultId(),
                entity.getSenderId(),
                entity.getSenderType(),
                entity.getContent(),
                entity.getContentType(),
                Boolean.TRUE.equals(entity.getReadFlag()),
                entity.getCreateTime()
            ))
            .toList();
    }
}

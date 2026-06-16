package com.hlw.common.mq.core;

import com.hlw.common.mq.model.MqMessage;

import java.util.List;

/**
 * 本地消息存储接口
 * 该接口定义了消息队列系统中本地消息存储的基本操作方法
 */
public interface MqLocalMessageStore {
    /**
     * 保存消息到本地存储
     * @param message 需要保存的消息对象
     */
    void save(MqMessage message);

    /**
     * 查询待处理的消息
     * @param limit 限制返回的消息数量
     * @return 待处理消息列表
     */
    List<MqMessage> findPending(int limit);

    /**
     * 标记消息为已发送状态
     * @param message 需要标记的消息对象
     */
    void markSent(MqMessage message);

    /**
     * 标记消息为发送失败状态
     * @param message 需要标记的消息对象
     * @param errorMessage 发送失败的错误信息
     */
    void markFailed(MqMessage message, String errorMessage);
}

package com.hlw.common.mq.service.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

/**
 * 基于 RocketMQ 的消息队列发送实现。
 *
 * @param <T> 消息类型
 * @param <P> 优先级/延迟时间类型（延迟秒数）
 */
@Slf4j
@Service
@ConditionalOnClass(RocketMQTemplate.class)
public class RocketMessageQueueSender<T, P> extends AbstractMessageQueueSender<T, P> {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    protected boolean doSend(String queue, T message) {
        String messageJson = super.serializeMessage(message);
        SendResult result = rocketMQTemplate.syncSend(queue, messageJson);
        log.info("RocketMQ Send Message ===> result={}, msg={}", result, messageJson);
        return SendStatus.SEND_OK.equals(result.getSendStatus());
    }

    @Override
    protected boolean broadSend(String channel, T message) {
        return this.doSend(channel, message);
    }

    @Override
    protected boolean prioritySend(String channel, T message, P priority) {
        String messageJson = super.serializeMessage(message);
        long delayTime = Long.parseLong(String.valueOf(priority));
        SendResult sendResult = rocketMQTemplate.syncSendDelayTimeSeconds(channel, messageJson, delayTime);
        return sendResult.getSendStatus() == SendStatus.SEND_OK;
    }
}

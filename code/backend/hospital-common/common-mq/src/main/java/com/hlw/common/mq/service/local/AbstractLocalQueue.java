package com.hlw.common.mq.service.local;

import com.hlw.common.core.util.JsonUtil;
import java.util.List;
import java.util.Queue;

import lombok.extern.slf4j.Slf4j;

/**
 * 本地队列抽象基类，提供入队、消费与拒绝策略。
 *
 * @param <Q> 队列实现类型
 * @param <M> 消息类型
 */
@Slf4j
public abstract class AbstractLocalQueue<Q extends Queue, M> {

    /**
     * 获取本地队列对象，由具体实现类创建。
     *
     * @return 队列实例
     */
    protected abstract Queue<M> queue();

    /**
     * 实际消费逻辑，由实现类完成。
     *
     * @param message 消息
     * @return 是否成功
     */
    protected abstract boolean consume(M message);

    /**
     * 入队异常拒绝策略。
     *
     * @param messages 消息列表
     * @return 是否成功
     */
    public abstract boolean doReject(List<M> messages);

    /**
     * 异常/处理失败后是否需要重新归队。
     *
     * @return 是否归队
     */
    protected boolean backQueue() {
        return true;
    }

    /**
     * 单条入队。
     *
     * @param message 消息
     * @return 是否成功
     */
    public boolean push(M message) {
        if (message == null) {
            return true;
        }
        return push(List.of(message));
    }

    /**
     * 批量入队。
     *
     * @param messages 消息列表
     * @return 是否成功
     */
    public boolean push(List<M> messages) {
        if (messages == null || messages.isEmpty()) {
            return true;
        }
        try {
            return queue().addAll(messages);
        } catch (Exception e) {
            log.error("本地队列入队异常, ", e);
            doReject(messages);
        }
        return true;
    }

    /**
     * 出队消费。
     *
     * @return 永不返回
     */
    public M execConsume() {
        while (true) {
            M message = null;
            try {
                message = (M) queue().poll();
                if (message != null) {
                    boolean res = consume(message);
                    if (!res && backQueue()) {
                        this.push(message);
                    }
                }
            } catch (Exception e) {
                log.error("通知消息队列消费异常！message:{}", JsonUtil.toJsonString(message), e);
                if (backQueue()) {
                    this.push(message);
                }
            }
        }
    }
}

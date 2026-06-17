package com.hlw.common.mq.service.local;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 基于 ArrayBlockingQueue 的本地队列骨架，子类按需实现队列实例与消费逻辑。
 *
 * @param <M> 消息类型
 */
public abstract class AbstractArrayBlockQueue<M> extends AbstractLocalQueue<ArrayBlockingQueue, M> {

    @Override
    protected Queue<M> queue() {
        return null;
    }

    @Override
    protected boolean consume(M message) {
        return false;
    }

    @Override
    public boolean doReject(List<M> messages) {
        return false;
    }
}

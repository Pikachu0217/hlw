package com.hlw.common.mq.service.consumer;

import com.hlw.common.core.constants.LUAScriptConstants;
import com.hlw.common.core.domain.queue.BaseDelayQueueDTO;
import com.hlw.common.mq.enums.MessageQueueEnum;
import com.hlw.common.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

import lombok.extern.slf4j.Slf4j;

/**
 * 抽象的 Redis 延迟队列消费者实现，基于 zset + Lua 保证消费原子性。
 *
 * @param <T> 消息类型
 */
@Slf4j
public abstract class AbstractRedisDelayQueueConsumer<T extends BaseDelayQueueDTO> {

    @Autowired
    protected RedisService redisService;

    private volatile boolean isInit = false;

    private volatile boolean isRun = true;

    private ThreadPoolExecutor poolExecutor;

    /**
     * 初始化线程池与日志输出。
     */
    public void init() {
        if (isInit) {
            return;
        }
        log.info("开始初始化消息队列，参数如下: \n" +
                        "queue name: {} \n" +
                        "queue timeout: {}\n" +
                        "queue retry times: {}\n" +
                        "queue back queue times: {}\n" +
                        "queue pool size: {}",
                getMessageQueue().getQueue(), getTimeout(), getRetryTimes(), getBackQueueTimes(), getPoolSize()
        );
        initThreadPool(getPoolSize());
        Long startSize = redisService.zSize(getQueueName());
        log.info("启动消息队列{}成功, 目前还有 {} 个元素没被消费", getQueueName(), startSize);
        isInit = true;
    }

    /**
     * 调度循环间隔（毫秒）。
     *
     * @return 等待时长
     */
    protected long getTimeout() {
        return 3000;
    }

    /**
     * 重试次数。
     *
     * @return 重试次数
     */
    protected int getRetryTimes() {
        return 0;
    }

    /**
     * 归队次数。
     *
     * @return 归队次数
     */
    protected int getBackQueueTimes() {
        return 0;
    }

    /**
     * 最大并发执行任务数。
     *
     * @return 线程池大小
     */
    protected int getPoolSize() {
        return 1;
    }

    /**
     * 字符串转业务对象。
     *
     * @param s 序列化字符串
     * @return 业务对象
     */
    protected abstract T convert(String s);

    /**
     * 队列枚举。
     *
     * @return 队列枚举
     */
    protected abstract MessageQueueEnum getMessageQueue();

    /**
     * 实际消费逻辑。
     *
     * @param messages 序列化消息集合
     */
    protected abstract void doNextConsume(Set<String> messages);

    /**
     * 队列名称。
     *
     * @return topic
     */
    protected String getQueueName() {
        return this.getMessageQueue().getQueue();
    }

    /**
     * 基于 Lua 脚本原子获取并删除到期消息。
     *
     * @return 到期消息集合
     */
    private Set<String> consumeMessage() {
        long currentTime = System.currentTimeMillis();
        List<String> execRes = redisService.executeScript(
                RedisScript.of(LUAScriptConstants.REDIS_DELAY_QUEUE_MESSAGE_CONSUME, List.class),
                List.of(getQueueName()),
                String.valueOf(Long.MIN_VALUE),
                String.valueOf(currentTime)
        );
        if (execRes == null || execRes.isEmpty()) {
            return Collections.emptySet();
        }
        return new LinkedHashSet<>(execRes);
    }

    /**
     * 延迟队列调度核心方法。
     */
    public void schedule() {
        init();
        try {
            final Semaphore semaphore = new Semaphore(getPoolSize());
            while (isRun) {
                Thread.sleep(1000);
                log.info("QueueName:{} 信号量剩余:{}", getQueueName(), semaphore.availablePermits());
                semaphore.acquire();
                final Set<String> messages = this.consumeMessage();
                if (messages == null || messages.isEmpty()) {
                    log.info("QueueName:{} 目前没有元素", getQueueName());
                    semaphore.release();
                    continue;
                }
                poolExecutor.execute(() -> {
                    try {
                        doNextConsume(messages);
                    } finally {
                        semaphore.release();
                    }
                });
            }
        } catch (final Exception outerEx) {
            log.error("启动消息队列失败, 原因 ", outerEx);
        }
    }

    /**
     * 初始化消费线程池。
     *
     * @param poolSize 线程数
     */
    private void initThreadPool(int poolSize) {
        if (poolExecutor == null) {
            synchronized (this) {
                if (poolExecutor == null) {
                    poolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(poolSize);
                }
            }
        }
    }
}

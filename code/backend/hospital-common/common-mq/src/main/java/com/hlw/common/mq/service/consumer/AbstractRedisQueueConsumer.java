package com.hlw.common.mq.service.consumer;

import com.hlw.common.core.domain.queue.BaseQueueDTO;
import com.hlw.common.core.util.JsonUtil;
import com.hlw.common.mq.enums.MessageQueueEnum;
import com.hlw.common.redis.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 抽象的 Redis 队列消费者，提供重试、归队、线程池调度等通用能力。
 *
 * @param <T> 消息类型
 */
@Slf4j
public abstract class AbstractRedisQueueConsumer<T extends BaseQueueDTO> {

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
                getQueueName(), getTimeout(), getRetryTimes(), getBackQueueTimes(), getPoolSize()
        );
        initThreadPool(getPoolSize());
        Long startSize = redisService.lSize(getQueueName());
        log.info("启动消息队列{}成功, 目前还有 {} 个元素没被消费", getQueueName(), startSize);
        isInit = true;
    }

    /**
     * 从消息队列 Redis 读取数据的延迟（毫秒）。
     *
     * @return 等待时长
     */
    protected long getTimeout() {
        return 3000;
    }

    /**
     * 一个任务元素重试的次数。
     *
     * @return 重试次数，默认不重试
     */
    protected int getRetryTimes() {
        return 0;
    }

    /**
     * 一个任务最多被重返队列的次数。
     *
     * @return 归队次数
     */
    protected int getBackQueueTimes() {
        return 0;
    }

    /**
     * 获取最大并发执行任务数量。
     *
     * @return 线程池大小
     */
    protected int getPoolSize() {
        return 1;
    }

    /**
     * 当一个服务不是主服务时下次检测的时间间隔（毫秒）。
     *
     * @return 等待时长
     */
    protected int getMasterWaitTime() {
        return 20000;
    }

    /**
     * 将字符串转化为对象。
     *
     * @param s 序列化字符串
     * @return 业务对象
     */
    protected abstract T convert(String s);

    /**
     * 默认 JSON 序列化。
     *
     * @param data 业务对象
     * @return 序列化字符串
     */
    protected String serializable(T data) {
        return JsonUtil.toJsonString(data);
    }

    /**
     * 队列枚举。
     *
     * @return 队列枚举
     */
    protected abstract MessageQueueEnum getMessageQueue();

    /**
     * 队列名称。
     *
     * @return topic
     */
    protected String getQueueName() {
        return getMessageQueue().getQueue();
    }

    /**
     * 监听到数据时的处理回调。
     *
     * @param data 业务对象
     */
    protected abstract void receive(T data);

    /**
     * 监听到错误时的处理回调。
     *
     * @param data 业务对象
     * @param ex 异常
     */
    protected abstract void error(T data, Exception ex);

    /**
     * consumer 重新归队实现。
     *
     * @param data 业务对象
     */
    protected void submit(T data) {
        String queueName = getQueueName();
        redisService.lLeftPush(queueName, serializable(data));
    }

    /**
     * 内部重试逻辑。
     *
     * @param data 业务对象
     * @return 是否重试成功
     */
    protected boolean retry(T data) {
        if (data == null) {
            return false;
        }
        if (data.getRetryTimes() < 0) {
            return false;
        }
        for (int i = 1; i <= getRetryTimes(); ++i) {
            data.setRetryTimes(i);
            try {
                log.info("{}开始执行第{}次重试", data.getClass(), i);
                receive(data);
                return true;
            } catch (Exception ex) {
                log.error("队列任务执行失败, 重试失败第{}次", i);
            }
        }
        return false;
    }

    /**
     * 将数据重新加入队列。
     *
     * @param data 业务对象
     * @return 是否归队成功
     */
    protected boolean backToQueue(T data) {
        if (data == null) {
            return false;
        }
        if (data.getBackQueueTimes() < getBackQueueTimes()) {
            log.error("数据重试失败，开始重新放入队列，已归队次数 {}", data.getBackQueueTimes());
            data.setBackQueueTimes(data.getBackQueueTimes() + 1);
            submit(data);
            return true;
        }
        return false;
    }

    /**
     * 队列调度核心方法。
     */
    public void schedule() {
        init();
        try {
            final Semaphore semaphore = new Semaphore(getPoolSize());
            while (isRun) {
                semaphore.acquire();
                final String s = redisService.lRightPop(getQueueName());
                if (!StringUtils.hasText(s)) {
                    semaphore.release();
                    Thread.sleep(getTimeout());
                    continue;
                }
                log.info("{} 中消费到消息：{}", getQueueName(), s);
                poolExecutor.execute(() -> {
                    T data = null;
                    try {
                        data = convert(s);
                        receive(data);
                    } catch (Exception ex) {
                        try {
                            if (!retry(data)) {
                                if (!backToQueue(data)) {
                                    error(data, ex);
                                }
                            }
                        } catch (Exception innerEx) {
                            log.error("消费队列异常捕获失败，原因: ", innerEx);
                        }
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

package com.hlw.common.mq.service.task;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hlw.common.core.enums.MessageQueueTaskStatusEnum;
import com.hlw.common.core.util.JsonUtil;
import com.hlw.common.core.util.TransactionSyncManagerUtil;
import com.hlw.common.mq.enums.MessageQueueEnum;
import com.hlw.common.mq.enums.MessageQueueMethodEnum;
import com.hlw.common.mq.service.producer.MessageQueueProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 消息队列失败任务调度抽象基类，由各业务模块实现具体持久化逻辑。
 *
 * @param <E> 业务持久化实体类型
 */
public abstract class AbstractMQTaskService<E> {

    @Autowired
    private MessageQueueProducer<String, Long> messageQueueProducer;

    /**
     * 分批查询失败任务条数。
     */
    protected static final int QUERY_MAX_SIZE = 1000;

    /**
     * 获取失败需要重新归队的任务列表，由各个服务自行实现。
     *
     * @return 任务列表
     */
    protected abstract List<MessageQueueTaskDTO> tasks();

    /**
     * 单条失败任务入库。
     *
     * @param task 任务
     * @return 入库数量
     */
    public int saveFailTask2DB(MessageQueueTaskDTO task) {
        if (task == null) {
            return 0;
        }
        return this.saveFailTask2DB(List.of(task));
    }

    /**
     * 批量保存 MQ 失败任务到数据库。
     *
     * @param tasks 任务列表
     * @return 入库数量
     */
    public abstract int saveFailTask2DB(List<MessageQueueTaskDTO> tasks);

    /**
     * 任务重新归队。
     *
     * @param task 任务
     */
    protected void backQueue(MessageQueueTaskDTO task) {
        String queueName = task.getTopicName();
        String message = task.getTaskJson();
        String methodName = task.getMethod();
        try {
            MessageQueueEnum queueEnum = MessageQueueEnum.getByQueue(queueName);
            if (MessageQueueMethodEnum.PRIORITY_SEND.getMethod().equals(methodName)) {
                Method method = messageQueueProducer.getClass().getDeclaredMethod(
                        methodName, MessageQueueEnum.class, Object.class, Object.class);
                method.invoke(messageQueueProducer, queueEnum, message, task.getPriority());
            } else if (MessageQueueMethodEnum.SEND.getMethod().equals(methodName)) {
                Method method = messageQueueProducer.getClass().getDeclaredMethod(
                        methodName, MessageQueueEnum.class, Object.class);
                method.invoke(messageQueueProducer, queueEnum, message);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 更新任务状态。
     *
     * @param taskIds 任务 ID 列表
     * @param status 状态
     */
    protected abstract void updateTaskStatus(List<String> taskIds, MessageQueueTaskStatusEnum status);

    /**
     * 将业务实体列表转换为通用 DTO。
     *
     * @param tasks 业务实体列表
     * @return DTO 列表
     */
    protected List<MessageQueueTaskDTO> convert(Collection<E> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return Collections.emptyList();
        }
        return JsonUtil.fromJson(JsonUtil.toJsonString(tasks), new TypeReference<List<MessageQueueTaskDTO>>() {
        });
    }

    /**
     * 失败任务归队执行入口。
     */
    @Transactional
    public void doExecute() {
        List<MessageQueueTaskDTO> tasks = this.tasks();
        if (tasks == null || tasks.isEmpty()) {
            return;
        }
        List<String> taskIds = tasks.stream().map(MessageQueueTaskDTO::getTaskId).toList();
        this.updateTaskStatus(taskIds, MessageQueueTaskStatusEnum.RUNNING);
        TransactionSyncManagerUtil.afterCommit(() -> {
            List<String> backQueueFailTask = new ArrayList<>();
            tasks.forEach(task -> {
                try {
                    this.backQueue(task);
                } catch (Exception e) {
                    backQueueFailTask.add(task.getTaskId());
                }
            });
            if (!backQueueFailTask.isEmpty()) {
                rollbackBackQueueFailTaskStatus(backQueueFailTask);
            }
        });
    }

    /**
     * 部分归队失败的任务状态回滚（独立事务）。
     *
     * @param backQueueFailTask 归队失败任务 ID 列表
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void rollbackBackQueueFailTaskStatus(List<String> backQueueFailTask) {
        updateTaskStatus(backQueueFailTask, MessageQueueTaskStatusEnum.ERROR);
    }
}

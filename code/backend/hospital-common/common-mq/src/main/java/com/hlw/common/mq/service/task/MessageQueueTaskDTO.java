package com.hlw.common.mq.service.task;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 消息队列失败任务 DTO，用于持久化失败消息以便归队重试。
 */
@Data
public class MessageQueueTaskDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    protected Long id;

    /**
     * 任务 ID。
     */
    private String taskId;

    /**
     * MQ 参数 JSON。
     */
    private String taskJson;

    /**
     * topic。
     */
    private String topicName;

    /**
     * 消息推送方式。
     *
     * @see com.hlw.common.mq.enums.MessageQueueMethodEnum
     */
    private String method;

    /**
     * 优先级或延迟时间。
     */
    private Long priority;

    /**
     * 消息状态。
     *
     * @see com.hlw.common.core.enums.MessageQueueTaskStatusEnum
     */
    private Integer status;

    /**
     * 任务创建时间。
     */
    private Date createDate;

    /**
     * 更新时间。
     */
    private Date updateDate;
}

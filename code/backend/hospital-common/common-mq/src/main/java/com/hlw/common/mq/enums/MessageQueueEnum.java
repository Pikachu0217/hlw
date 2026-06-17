package com.hlw.common.mq.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 业务消息队列 topic 枚举。
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum MessageQueueEnum {

    QUEUE_ORDER_PAID("queue_order_paid", "订单支付完成消息"),

    QUEUE_DRUG_SHIPPED("queue_drug_shipped", "药品配送发货消息"),

    QUEUE_PRESCRIPTION_AUDITED("queue_prescription_audited", "处方审核通过消息"),
    ;

    private String queue;

    private String queueName;

    /**
     * 根据 topic 名称查询枚举。
     *
     * @param queue topic 名称
     * @return 队列枚举，未匹配返回 null
     */
    public static MessageQueueEnum getByQueue(String queue) {
        for (MessageQueueEnum messageQueueEnum : MessageQueueEnum.values()) {
            if (messageQueueEnum.getQueue().equals(queue)) {
                return messageQueueEnum;
            }
        }
        return null;
    }
}

package com.hlw.common.mq.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 广播频道枚举。
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum MessageBroadChannelEnum {

    QUEUE_ORDER_NOTIFY("queue_order_notify", "订单通知广播频道"),
    ;

    private String channel;

    private String channelDesc;
}

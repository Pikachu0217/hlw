package com.hlw.common.mq.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 消息队列模块自动装配，自动扫描 common-mq 内的发送者、消费者基类等组件。
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.hlw.common.mq")
public class MessageQueueAutoConfiguration {
}

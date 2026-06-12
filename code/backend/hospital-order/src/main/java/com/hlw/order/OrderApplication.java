package com.hlw.order;

import com.hlw.common.mq.config.LocalMqConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * 订单服务启动入口。
 */
@SpringBootApplication
@Import(LocalMqConfig.class)
public class OrderApplication {
    /**
     * 启动订单服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}

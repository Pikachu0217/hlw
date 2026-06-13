package com.hlw.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 订单服务启动入口。
 */
@SpringBootApplication(scanBasePackages = "com.hlw")
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

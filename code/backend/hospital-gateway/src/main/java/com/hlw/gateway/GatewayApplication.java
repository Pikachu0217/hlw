package com.hlw.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 网关服务启动入口。
 */
@SpringBootApplication(scanBasePackages = "com.hlw")
public class GatewayApplication {
    /**
     * 启动网关服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}

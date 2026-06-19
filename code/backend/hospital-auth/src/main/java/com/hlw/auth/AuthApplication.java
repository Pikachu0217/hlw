package com.hlw.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 认证服务启动入口。
 */
@SpringBootApplication(scanBasePackages = "com.hlw", exclude = DataSourceAutoConfiguration.class)
@EnableFeignClients(basePackages = "com.hlw.auth.client")
public class AuthApplication {
    /**
     * 启动认证服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}

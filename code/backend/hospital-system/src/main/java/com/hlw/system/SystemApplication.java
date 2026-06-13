package com.hlw.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 系统服务启动入口。
 */
@SpringBootApplication(scanBasePackages = "com.hlw")
public class SystemApplication {
    /**
     * 启动系统服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class, args);
    }
}

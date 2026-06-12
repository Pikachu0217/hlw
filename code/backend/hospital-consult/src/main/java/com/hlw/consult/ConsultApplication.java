package com.hlw.consult;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 问诊服务启动入口。
 */
@SpringBootApplication
public class ConsultApplication {
    /**
     * 启动问诊服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ConsultApplication.class, args);
    }
}

package com.hlw.prescription;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 处方服务启动入口。
 */
@SpringBootApplication(scanBasePackages = "com.hlw")
public class PrescriptionApplication {
    /**
     * 启动处方服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(PrescriptionApplication.class, args);
    }
}

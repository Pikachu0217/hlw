package com.hlw.patient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 患者服务启动入口。
 */
@SpringBootApplication(scanBasePackages = "com.hlw")
public class PatientApplication {
    /**
     * 启动患者服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(PatientApplication.class, args);
    }
}

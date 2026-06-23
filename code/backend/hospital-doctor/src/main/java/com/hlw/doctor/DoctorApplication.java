package com.hlw.doctor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 医生服务启动入口。
 */
@SpringBootApplication(scanBasePackages = "com.hlw")
@EnableFeignClients(basePackages = "com.hlw.doctor.client")
public class DoctorApplication {
    /**
     * 启动医生服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(DoctorApplication.class, args);
    }
}

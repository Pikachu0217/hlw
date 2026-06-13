package com.hlw.appointment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 预约服务启动入口。
 */
@SpringBootApplication(scanBasePackages = "com.hlw")
public class AppointmentApplication {
    /**
     * 启动预约服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(AppointmentApplication.class, args);
    }
}

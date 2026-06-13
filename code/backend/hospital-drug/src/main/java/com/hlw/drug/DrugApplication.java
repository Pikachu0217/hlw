package com.hlw.drug;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 药品服务启动入口。
 */
@SpringBootApplication(scanBasePackages = "com.hlw")
public class DrugApplication {
    /**
     * 启动药品服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(DrugApplication.class, args);
    }
}

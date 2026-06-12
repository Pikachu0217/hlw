package com.hlw.prescription;

import com.hlw.common.mq.config.LocalMqConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * 处方服务启动入口。
 */
@SpringBootApplication
@Import(LocalMqConfig.class)
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

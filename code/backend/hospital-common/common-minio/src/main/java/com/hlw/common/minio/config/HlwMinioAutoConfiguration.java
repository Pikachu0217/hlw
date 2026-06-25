package com.hlw.common.minio.config;

import com.hlw.common.minio.service.HlwMinioService;
import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * MinIO 自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(HlwMinioProperties.class)
public class HlwMinioAutoConfiguration {

    /**
     * 创建 MinIO 客户端。
     *
     * @param properties MinIO 配置属性
     * @return MinIO 客户端
     */
    @Bean
    @ConditionalOnMissingBean
    public MinioClient minioClient(HlwMinioProperties properties) {
        return MinioClient.builder()
            .endpoint(properties.getEndpoint())
            .credentials(properties.getAccessKey(), properties.getSecretKey())
            .build();
    }

    /**
     * 创建 MinIO 文件存储服务。
     *
     * @param minioClient MinIO 客户端
     * @param properties MinIO 配置属性
     * @return MinIO 文件存储服务
     */
    @Bean
    @ConditionalOnMissingBean
    public HlwMinioService hlwMinioService(MinioClient minioClient, HlwMinioProperties properties) {
        return new HlwMinioService(minioClient, properties);
    }
}

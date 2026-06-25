package com.hlw.common.minio.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MinIO 配置属性。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "hlw.minio")
public class HlwMinioProperties {
    /** MinIO 内部访问地址。 */
    private String endpoint = "http://127.0.0.1:9000";
    /** MinIO 外部公开访问地址。 */
    private String publicEndpoint = "http://127.0.0.1:9000";
    /** MinIO 访问密钥。 */
    private String accessKey = "minio";
    /** MinIO 访问密钥密码。 */
    private String secretKey = "minio123";
    /** 默认存储桶名称。 */
    private String bucket = "consult-images";
}

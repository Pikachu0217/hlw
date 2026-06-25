package com.hlw.common.minio.service;

import com.hlw.common.core.exception.BizException;
import com.hlw.common.minio.config.HlwMinioProperties;
import com.hlw.common.minio.domain.MinioDownloadResult;
import com.hlw.common.minio.domain.MinioUploadResult;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * MinIO 文件存储服务。
 */
@RequiredArgsConstructor
@Slf4j
public class HlwMinioService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** MinIO 客户端。 */
    private final MinioClient minioClient;
    /** MinIO 配置属性。 */
    private final HlwMinioProperties properties;

    /**
     * 上传公开读取文件。
     *
     * @param file 上传文件
     * @param bizDir 业务目录
     * @return 上传结果
     */
    public MinioUploadResult uploadPublicFile(MultipartFile file, String bizDir) {
        if (file == null || file.isEmpty()) {
            throw new BizException(400, "上传文件不能为空");
        }
        String bucket = normalizeBucket(properties.getBucket());
        String objectName = buildObjectName(bizDir, file);
        try {
            ensurePublicBucket(bucket);
            minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .contentType(resolveContentType(file))
                .stream(file.getInputStream(), file.getSize(), -1)
                .build());
            String url = buildPublicUrl(bucket, objectName);
            log.info("MinIO 文件上传成功，bucket={}，objectName={}，size={}", bucket, objectName, file.getSize());
            return new MinioUploadResult(bucket, objectName, url);
        } catch (BizException exception) {
            throw exception;
        } catch (Exception exception) {
            log.warn("MinIO 文件上传失败，bucket={}，objectName={}", bucket, objectName, exception);
            throw new BizException(500, "文件上传失败");
        }
    }

    /**
     * 读取公开文件。
     *
     * @param objectName 对象名称
     * @return 下载结果
     */
    public MinioDownloadResult readPublicFile(String objectName) {
        String bucket = normalizeBucket(properties.getBucket());
        String normalizedObjectName = normalizeObjectName(objectName);
        try {
            StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                .bucket(bucket)
                .object(normalizedObjectName)
                .build());
            byte[] bytes;
            try (var stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(normalizedObjectName)
                    .build())) {
                bytes = stream.readAllBytes();
            }
            log.info("MinIO 文件读取成功，bucket={}，objectName={}，size={}", bucket, normalizedObjectName, bytes.length);
            return new MinioDownloadResult(normalizedObjectName, stat.contentType(), bytes);
        } catch (BizException exception) {
            throw exception;
        } catch (Exception exception) {
            log.warn("MinIO 文件读取失败，bucket={}，objectName={}", bucket, normalizedObjectName, exception);
            throw new BizException(404, "图片文件不存在");
        }
    }

    /**
     * 确保存储桶存在并允许公开读取。
     *
     * @param bucket 存储桶名称
     */
    private void ensurePublicBucket(String bucket) throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            log.info("MinIO 存储桶已创建，bucket={}", bucket);
        }
        minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
            .bucket(bucket)
            .config(publicReadPolicy(bucket))
            .build());
    }

    /**
     * 构造公开读取策略。
     *
     * @param bucket 存储桶名称
     * @return 策略 JSON
     */
    private String publicReadPolicy(String bucket) {
        return """
            {
              "Version": "2012-10-17",
              "Statement": [
                {
                  "Effect": "Allow",
                  "Principal": {"AWS": ["*"]},
                  "Action": ["s3:GetObject"],
                  "Resource": ["arn:aws:s3:::%s/*"]
                }
              ]
            }
            """.formatted(bucket);
    }

    /**
     * 构造对象名称。
     *
     * @param bizDir 业务目录
     * @param file 上传文件
     * @return 对象名称
     */
    private String buildObjectName(String bizDir, MultipartFile file) {
        String dir = bizDir == null || bizDir.isBlank() ? "default" : bizDir.replaceAll("[^a-zA-Z0-9/_-]", "");
        String date = LocalDate.now().format(DATE_FORMATTER);
        return dir + "/" + date + "/" + UUID.randomUUID().toString().replace("-", "") + resolveExtension(file);
    }

    /**
     * 解析文件扩展名。
     *
     * @param file 上传文件
     * @return 扩展名
     */
    private String resolveExtension(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        if (originalName != null) {
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < originalName.length() - 1) {
                String extension = originalName.substring(dotIndex).toLowerCase(Locale.ROOT);
                if (extension.length() <= 10 && extension.matches("\\.[a-z0-9]+")) {
                    return extension;
                }
            }
        }
        String contentType = resolveContentType(file);
        if ("image/png".equalsIgnoreCase(contentType)) {
            return ".png";
        }
        if ("image/gif".equalsIgnoreCase(contentType)) {
            return ".gif";
        }
        if ("image/webp".equalsIgnoreCase(contentType)) {
            return ".webp";
        }
        return ".jpg";
    }

    /**
     * 解析内容类型。
     *
     * @param file 上传文件
     * @return 内容类型
     */
    private String resolveContentType(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType;
    }

    /**
     * 构造公开访问地址。
     *
     * @param bucket 存储桶名称
     * @param objectName 对象名称
     * @return 公开访问地址
     */
    private String buildPublicUrl(String bucket, String objectName) {
        String endpoint = properties.getPublicEndpoint();
        String normalizedEndpoint = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        return normalizedEndpoint + "/" + bucket + "/" + objectName;
    }

    /**
     * 规范化存储桶名称。
     *
     * @param bucket 存储桶名称
     * @return 存储桶名称
     */
    private String normalizeBucket(String bucket) {
        if (bucket == null || bucket.isBlank()) {
            throw new BizException(500, "MinIO 存储桶未配置");
        }
        return bucket.trim();
    }

    /**
     * 规范化对象名称。
     *
     * @param objectName 对象名称
     * @return 对象名称
     */
    private String normalizeObjectName(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            throw new BizException(400, "MinIO 对象名称不能为空");
        }
        String normalizedObjectName = objectName.trim();
        if (normalizedObjectName.startsWith("/") || normalizedObjectName.contains("..")) {
            throw new BizException(400, "MinIO 对象名称不合法");
        }
        return normalizedObjectName;
    }
}

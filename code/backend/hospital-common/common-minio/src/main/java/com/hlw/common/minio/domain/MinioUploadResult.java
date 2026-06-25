package com.hlw.common.minio.domain;

/**
 * MinIO 上传结果。
 *
 * @param bucket 存储桶名称
 * @param objectName 对象名称
 * @param url 公开访问地址
 */
public record MinioUploadResult(
    String bucket,
    String objectName,
    String url
) {
}

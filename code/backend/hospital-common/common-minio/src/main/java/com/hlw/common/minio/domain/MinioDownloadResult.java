package com.hlw.common.minio.domain;

/**
 * MinIO 下载结果。
 *
 * @param objectName 对象名称
 * @param contentType 内容类型
 * @param bytes 文件字节
 */
public record MinioDownloadResult(
    String objectName,
    String contentType,
    byte[] bytes
) {
}

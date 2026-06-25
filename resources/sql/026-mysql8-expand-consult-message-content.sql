-- =============================================================
-- 026 — 扩展问诊图片消息 URL 长度
-- 改动：支持保存 MinIO 图片访问地址。
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `hospital_consult`;

ALTER TABLE `con_message`
    MODIFY COLUMN `content` varchar(1024) DEFAULT NULL COMMENT '消息内容（文本内容或 MinIO 图片地址）';

SET FOREIGN_KEY_CHECKS = 1;

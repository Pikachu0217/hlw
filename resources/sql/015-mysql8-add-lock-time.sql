-- =============================================================
-- 015 — apt_number_source 补齐 lock_time 字段
-- 该字段在 AptNumberSourceEntity 中定义但建表时遗漏
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `hospital_appointment`;

ALTER TABLE `apt_number_source`
    ADD COLUMN `lock_time` datetime DEFAULT NULL COMMENT '锁定时间' AFTER `status`;

SET FOREIGN_KEY_CHECKS = 1;

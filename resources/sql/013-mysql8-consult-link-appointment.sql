-- =============================================================
-- 013 — 问诊单关联预约单 & 支付状态
-- 改动：con_consult 新增 appointment_id、pay_status 字段
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. con_consult 新增字段（hospital_consult 库）
-- ----------------------------
USE `hospital_consult`;

ALTER TABLE `con_consult`
    ADD COLUMN `appointment_id` bigint DEFAULT NULL COMMENT '关联预约单编号' AFTER `doctor_id`,
    ADD COLUMN `pay_status` varchar(20) NOT NULL DEFAULT 'UNPAID' COMMENT '支付状态（UNPAID=未支付, PAID=已支付）' AFTER `status`,
    ADD INDEX `idx_appointment_id` (`appointment_id`) COMMENT '预约单编号索引';

-- ----------------------------
-- 2. 更新已有问诊单的演示数据
-- ----------------------------
UPDATE `con_consult` SET `pay_status` = 'PAID' WHERE `id` IN (2, 3);
UPDATE `con_consult` SET `pay_status` = 'UNPAID' WHERE `id` IN (4, 5);

SET FOREIGN_KEY_CHECKS = 1;

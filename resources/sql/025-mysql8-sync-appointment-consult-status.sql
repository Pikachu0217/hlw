-- =============================================================
-- 025 — 预约与问诊状态联动
-- 改动：补充拒诊字段，约束预约与问诊一对一关联。
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ========================================
-- 数据库：hospital_appointment — 预约拒诊字段
-- ========================================
USE `hospital_appointment`;

ALTER TABLE `apt_appointment`
    MODIFY COLUMN `status` varchar(20) NOT NULL DEFAULT '待支付' COMMENT '预约状态（待支付/已支付/已签到/已完成/已取消/已拒诊/已接单）';

ALTER TABLE `apt_appointment`
    ADD COLUMN `reject_time` datetime DEFAULT NULL COMMENT '拒诊时间'
    AFTER `cancel_reason`;

ALTER TABLE `apt_appointment`
    ADD COLUMN `reject_reason` varchar(256) NOT NULL DEFAULT '' COMMENT '拒诊原因'
    AFTER `reject_time`;

-- ========================================
-- 数据库：hospital_consult — 问诊拒诊字段与一对一索引
-- ========================================
USE `hospital_consult`;

ALTER TABLE `con_consult`
    MODIFY COLUMN `status` varchar(20) NOT NULL DEFAULT '0' COMMENT '问诊状态（0=待接单,1=咨询中,2=已延长,3=已完成,4=已取消,5=已超时,6=已拒诊）';

ALTER TABLE `con_consult`
    ADD COLUMN `reject_time` datetime DEFAULT NULL COMMENT '拒诊时间'
    AFTER `end_time`;

ALTER TABLE `con_consult`
    ADD COLUMN `reject_reason` varchar(256) NOT NULL DEFAULT '' COMMENT '拒诊原因'
    AFTER `reject_time`;

-- 历史数据如存在同一预约关联多条问诊，保留最早一条，其余解除关联，避免唯一索引创建失败。
UPDATE `con_consult` c
JOIN (
    SELECT `appointment_id`, MIN(`id`) AS `keep_id`
    FROM `con_consult`
    WHERE `appointment_id` IS NOT NULL
    GROUP BY `appointment_id`
    HAVING COUNT(*) > 1
) d ON c.`appointment_id` = d.`appointment_id`
SET c.`appointment_id` = NULL
WHERE c.`id` <> d.`keep_id`;

ALTER TABLE `con_consult`
    DROP INDEX `idx_appointment_id`;

ALTER TABLE `con_consult`
    ADD UNIQUE INDEX `uk_appointment_id` (`appointment_id`) COMMENT '预约单编号唯一索引';

SET FOREIGN_KEY_CHECKS = 1;

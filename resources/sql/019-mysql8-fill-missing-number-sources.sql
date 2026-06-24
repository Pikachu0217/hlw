-- =============================================================
-- 019 — 为缺少号源的排班补全号源数据
-- 修复因 014 表结构错误导致 Feign 创建号源失败的问题
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `hospital_appointment`;

-- 找出 doc_schedule 中有但 apt_number_source 中没有号源的排班
-- 对每个这样的排班，生成 totalNumber 个可用号源
-- 同时补全 apt_number_source_release_config

-- 排班 1（doctorId=3, tenant=0）：total_number=2
INSERT IGNORE INTO `apt_number_source` (`tenant_id`, `schedule_id`, `number_seq`, `status`, `create_time`, `update_time`, `deleted`)
SELECT 0, 1, seq, 'AVAILABLE', NOW(), NOW(), 0
FROM (SELECT 1 AS seq UNION SELECT 2) AS nums
WHERE NOT EXISTS (SELECT 1 FROM `apt_number_source` WHERE `schedule_id` = 1 LIMIT 1);

-- 排班 2（doctorId=4, tenant=17822850632175495）：total_number=30
INSERT IGNORE INTO `apt_number_source` (`tenant_id`, `schedule_id`, `number_seq`, `status`, `create_time`, `update_time`, `deleted`)
SELECT 17822850632175495, 2, seq, 'AVAILABLE', NOW(), NOW(), 0
FROM (
    SELECT 1 AS seq UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
    UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
    UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
    UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 UNION SELECT 25
    UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29 UNION SELECT 30
) AS nums
WHERE NOT EXISTS (SELECT 1 FROM `apt_number_source` WHERE `schedule_id` = 2 LIMIT 1);

-- 排班 3（doctorId=5, tenant=17822850632175495）：total_number=30
INSERT IGNORE INTO `apt_number_source` (`tenant_id`, `schedule_id`, `number_seq`, `status`, `create_time`, `update_time`, `deleted`)
SELECT 17822850632175495, 3, seq, 'AVAILABLE', NOW(), NOW(), 0
FROM (
    SELECT 1 AS seq UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
    UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
    UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
    UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 UNION SELECT 25
    UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29 UNION SELECT 30
) AS nums
WHERE NOT EXISTS (SELECT 1 FROM `apt_number_source` WHERE `schedule_id` = 3 LIMIT 1);

-- 排班 4+（后续通过管理端创建的排班）：动态从 doc_schedule 补齐
-- 查询 hospital_doctor 库获取排班数据
-- 使用跨库 INSERT ... SELECT
INSERT IGNORE INTO `apt_number_source` (`tenant_id`, `schedule_id`, `number_seq`, `status`, `create_time`, `update_time`, `deleted`)
SELECT
    COALESCE(s.`tenant_id`, 0),
    s.`id`,
    seq.n,
    'AVAILABLE',
    NOW(),
    NOW(),
    0
FROM `hospital_doctor`.`doc_schedule` s
CROSS JOIN (
    SELECT 1 AS n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
    UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
    UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
    UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 UNION SELECT 25
    UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29 UNION SELECT 30
) seq
WHERE seq.n <= s.`total_number`
AND NOT EXISTS (
    SELECT 1 FROM `apt_number_source` ns
    WHERE ns.`schedule_id` = s.`id` LIMIT 1
);

-- 补齐放号配置（幂等）
INSERT IGNORE INTO `apt_number_source_release_config` (`tenant_id`, `schedule_id`, `release_time`, `release_count`, `status`, `create_time`, `update_time`, `deleted`)
SELECT
    COALESCE(s.`tenant_id`, 0),
    s.`id`,
    NOW(),
    s.`total_number`,
    '启用',
    NOW(),
    NOW(),
    0
FROM `hospital_doctor`.`doc_schedule` s
WHERE NOT EXISTS (
    SELECT 1 FROM `apt_number_source_release_config` rc
    WHERE rc.`schedule_id` = s.`id` LIMIT 1
);

SET FOREIGN_KEY_CHECKS = 1;

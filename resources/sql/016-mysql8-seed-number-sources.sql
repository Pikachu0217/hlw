-- =============================================================
-- 016 — 为已有排班生成号源演示数据
-- 排班 1: 总号数 2，排班 2/3: 总号数 30
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `hospital_appointment`;

-- 清空已有号源（幂等：重复执行时先删后插）
DELETE FROM `apt_number_source` WHERE `schedule_id` IN (1, 2, 3);

-- 排班 1：2 个号（doctorId=3, tenant_id=0）
INSERT INTO `apt_number_source` (`tenant_id`, `schedule_id`, `number_seq`, `status`, `create_time`, `update_time`, `deleted`)
VALUES
    (0, 1, 1, 'AVAILABLE', NOW(), NOW(), 0),
    (0, 1, 2, 'AVAILABLE', NOW(), NOW(), 0);

-- 排班 2：30 个号（doctorId=4, tenant_id=17822850632175495）
INSERT INTO `apt_number_source` (`tenant_id`, `schedule_id`, `number_seq`, `status`, `create_time`, `update_time`, `deleted`)
SELECT
    17822850632175495,
    2,
    seq,
    'AVAILABLE',
    NOW(),
    NOW(),
    0
FROM (
    SELECT 1 AS seq UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
    UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
    UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
    UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 UNION SELECT 25
    UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29 UNION SELECT 30
) AS numbers;

-- 排班 3：30 个号（doctorId=5, tenant_id=17822850632175495）
INSERT INTO `apt_number_source` (`tenant_id`, `schedule_id`, `number_seq`, `status`, `create_time`, `update_time`, `deleted`)
SELECT
    17822850632175495,
    3,
    seq,
    'AVAILABLE',
    NOW(),
    NOW(),
    0
FROM (
    SELECT 1 AS seq UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
    UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
    UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
    UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 UNION SELECT 25
    UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29 UNION SELECT 30
) AS numbers;

-- 同时清理已有的放号配置（如有），保证后续创建不重复
DELETE FROM `apt_number_source_release_config` WHERE `schedule_id` IN (1, 2, 3);

INSERT INTO `apt_number_source_release_config` (`tenant_id`, `schedule_id`, `release_time`, `release_count`, `status`, `create_time`, `update_time`, `deleted`)
VALUES
    (0, 1, NOW(), 2,  '启用', NOW(), NOW(), 0),
    (17822850632175495, 2, NOW(), 30, '启用', NOW(), NOW(), 0),
    (17822850632175495, 3, NOW(), 30, '启用', NOW(), NOW(), 0);

SET FOREIGN_KEY_CHECKS = 1;

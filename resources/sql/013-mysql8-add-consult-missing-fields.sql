-- 问诊单表补充缺失字段脚本。
-- 执行方式示例：mysql -u root -p < resources/sql/013-mysql8-add-consult-missing-fields.sql
-- 说明：脚本可重复执行，补齐 con_consult 的 consult_no、patient_name、doctor_name、channel、updated_at 字段。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE hospital_consult;

DROP PROCEDURE IF EXISTS migrate_consult_missing_fields;

DELIMITER $$
CREATE PROCEDURE migrate_consult_missing_fields()
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'con_consult'
          AND column_name = 'consult_no'
    ) THEN
        ALTER TABLE `con_consult`
            ADD COLUMN `consult_no` varchar(64) NOT NULL DEFAULT '' COMMENT '问诊单号' AFTER `consult_type`;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'con_consult'
          AND column_name = 'patient_name'
    ) THEN
        ALTER TABLE `con_consult`
            ADD COLUMN `patient_name` varchar(100) NOT NULL DEFAULT '' COMMENT '患者姓名' AFTER `doctor_id`;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'con_consult'
          AND column_name = 'doctor_name'
    ) THEN
        ALTER TABLE `con_consult`
            ADD COLUMN `doctor_name` varchar(100) NOT NULL DEFAULT '' COMMENT '医生姓名' AFTER `patient_name`;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'con_consult'
          AND column_name = 'channel'
    ) THEN
        ALTER TABLE `con_consult`
            ADD COLUMN `channel` varchar(32) NOT NULL DEFAULT '' COMMENT '问诊渠道' AFTER `doctor_name`;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'con_consult'
          AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE `con_consult`
            ADD COLUMN `updated_at` varchar(32) NOT NULL DEFAULT '' COMMENT '前端展示更新时间' AFTER `remaining_seconds`;
    END IF;
END$$
DELIMITER ;

CALL migrate_consult_missing_fields();

DROP PROCEDURE IF EXISTS migrate_consult_missing_fields;

SET FOREIGN_KEY_CHECKS = 1;

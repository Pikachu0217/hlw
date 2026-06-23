-- 医生展示字段补齐脚本。
-- 执行方式示例：mysql -u root -p < resources/sql/009-mysql8-complete-doctor-display-fields.sql
-- 说明：脚本可重复执行，补齐医生列表接口依赖的展示字段。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE hospital_doctor;

DROP PROCEDURE IF EXISTS migrate_doctor_display_fields;

DELIMITER $$
CREATE PROCEDURE migrate_doctor_display_fields()
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'doc_doctor'
          AND column_name = 'doctor_name'
    ) THEN
        ALTER TABLE `doc_doctor`
            ADD COLUMN `doctor_name` varchar(64) NOT NULL DEFAULT '' COMMENT '医生姓名' AFTER `name`;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'doc_doctor'
          AND column_name = 'department'
    ) THEN
        ALTER TABLE `doc_doctor`
            ADD COLUMN `department` varchar(64) NOT NULL DEFAULT '' COMMENT '所属科室' AFTER `title`;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'doc_doctor'
          AND column_name = 'status'
    ) THEN
        ALTER TABLE `doc_doctor`
            ADD COLUMN `status` varchar(32) NOT NULL DEFAULT '接诊中' COMMENT '展示状态' AFTER `rating`;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'doc_doctor'
          AND column_name = 'schedule_desc'
    ) THEN
        ALTER TABLE `doc_doctor`
            ADD COLUMN `schedule_desc` varchar(128) NOT NULL DEFAULT '今日暂无排班' COMMENT '排班描述' AFTER `status`;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'doc_doctor'
          AND column_name = 'patient_count'
    ) THEN
        ALTER TABLE `doc_doctor`
            ADD COLUMN `patient_count` int NOT NULL DEFAULT 0 COMMENT '当前接诊患者数' AFTER `schedule_desc`;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'doc_doctor'
          AND column_name = 'consult_status'
          AND data_type <> 'varchar'
    ) THEN
        ALTER TABLE `doc_doctor`
            MODIFY COLUMN `consult_status` varchar(32) NOT NULL DEFAULT 'OFFLINE' COMMENT '接诊状态';
    END IF;

    UPDATE `doc_doctor`
    SET `doctor_name` = `name`
    WHERE `doctor_name` = ''
      AND `name` <> '';

    UPDATE `doc_doctor`
    SET `consult_status` = CASE `consult_status`
        WHEN '0' THEN 'OFFLINE'
        WHEN '1' THEN 'ONLINE'
        WHEN '2' THEN 'BUSY'
        ELSE `consult_status`
    END;

    UPDATE `doc_doctor`
    SET `status` = CASE UPPER(`consult_status`)
        WHEN 'ONLINE' THEN '接诊中'
        WHEN 'BUSY' THEN '忙碌'
        WHEN 'OFFLINE' THEN '停诊'
        ELSE `status`
    END
    WHERE `status` = ''
       OR `status` IN ('0', '1', '2');
END$$
DELIMITER ;

CALL migrate_doctor_display_fields();

DROP PROCEDURE IF EXISTS migrate_doctor_display_fields;

SET FOREIGN_KEY_CHECKS = 1;

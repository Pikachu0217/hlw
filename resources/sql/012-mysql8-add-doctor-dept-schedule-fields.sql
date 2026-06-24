-- 医生科室与排班字段补齐脚本。
-- 执行方式示例：mysql -u root -p < resources/sql/012-mysql8-add-doctor-dept-schedule-fields.sql
-- 说明：脚本可重复执行，补齐科室扩展系统部门编号和排班科室编号。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE hospital_doctor;

DROP PROCEDURE IF EXISTS migrate_doctor_dept_schedule_fields;

DELIMITER $$
CREATE PROCEDURE migrate_doctor_dept_schedule_fields()
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'doc_department'
          AND column_name = 'dept_id'
    ) THEN
        ALTER TABLE `doc_department`
            ADD COLUMN `dept_id` bigint COMMENT '系统部门编号' AFTER `id`;
    END IF;

    UPDATE `doc_department`
    SET `dept_id` = `id`
    WHERE `dept_id` IS NULL;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'doc_doctor_department'
          AND column_name = 'dept_id'
    ) THEN
        ALTER TABLE `doc_doctor_department`
            ADD COLUMN `dept_id` bigint COMMENT '科室编号' AFTER `doctor_id`;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'doc_doctor_department'
          AND column_name = 'department_id'
    ) THEN
        UPDATE `doc_doctor_department`
        SET `dept_id` = `department_id`
        WHERE `dept_id` IS NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'doc_schedule'
          AND column_name = 'dept_id'
    ) THEN
        ALTER TABLE `doc_schedule`
            ADD COLUMN `dept_id` bigint COMMENT '科室编号' AFTER `doctor_id`;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'doc_schedule'
          AND column_name = 'department_id'
    ) THEN
        UPDATE `doc_schedule`
        SET `dept_id` = `department_id`
        WHERE `dept_id` IS NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'doc_schedule'
          AND column_name = 'slot'
    ) THEN
        ALTER TABLE `doc_schedule`
            ADD COLUMN `slot` varchar(128) NOT NULL DEFAULT '' COMMENT '出诊时段' AFTER `tenant_id`;
    END IF;
END$$
DELIMITER ;

CALL migrate_doctor_dept_schedule_fields();

DROP PROCEDURE IF EXISTS migrate_doctor_dept_schedule_fields;

SET FOREIGN_KEY_CHECKS = 1;

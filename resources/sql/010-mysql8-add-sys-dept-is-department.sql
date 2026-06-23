-- 系统部门科室标记补齐脚本。
-- 执行方式示例：mysql -u root -p < resources/sql/010-mysql8-add-sys-dept-is-department.sql
-- 说明：脚本可重复执行，为医疗资源科室列表提供基础科室来源。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE hospital_system;

DROP PROCEDURE IF EXISTS migrate_sys_dept_is_department;

DELIMITER $$
CREATE PROCEDURE migrate_sys_dept_is_department()
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'sys_dept'
          AND column_name = 'is_department'
    ) THEN
        ALTER TABLE `sys_dept`
            ADD COLUMN `is_department` tinyint NOT NULL DEFAULT 0 COMMENT '是否科室（0=否，1=是）' AFTER `email`;
    END IF;

    INSERT INTO `sys_dept` (`tenant_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `is_department`, `status`, `deleted`, `create_time`, `update_time`)
    SELECT '0', 0, '0', '全科', 1, '', '', '', 1, 0, 0, NOW(), NOW()
    FROM DUAL
    WHERE NOT EXISTS (
        SELECT 1
        FROM `sys_dept`
        WHERE `tenant_id` = '0'
          AND `dept_name` = '全科'
          AND `deleted` = 0
    );

    INSERT INTO `sys_dept` (`tenant_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `is_department`, `status`, `deleted`, `create_time`, `update_time`)
    SELECT '0', 0, '0', '皮肤科', 2, '', '', '', 1, 0, 0, NOW(), NOW()
    FROM DUAL
    WHERE NOT EXISTS (
        SELECT 1
        FROM `sys_dept`
        WHERE `tenant_id` = '0'
          AND `dept_name` = '皮肤科'
          AND `deleted` = 0
    );

    UPDATE `sys_user` user_table
    JOIN `sys_dept` dept_table
      ON dept_table.`tenant_id` = user_table.`tenant_id`
     AND dept_table.`dept_name` = CASE user_table.`user_name`
        WHEN 'doctor_chen' THEN '全科'
        WHEN 'doctor_gu' THEN '皮肤科'
        ELSE dept_table.`dept_name`
     END
     AND dept_table.`deleted` = 0
    SET user_table.`dept_id` = dept_table.`id`
    WHERE user_table.`tenant_id` = '0'
      AND user_table.`user_type` = 'doctor'
      AND user_table.`deleted` = 0
      AND user_table.`user_name` IN ('doctor_chen', 'doctor_gu');
END$$
DELIMITER ;

CALL migrate_sys_dept_is_department();

DROP PROCEDURE IF EXISTS migrate_sys_dept_is_department;

SET FOREIGN_KEY_CHECKS = 1;

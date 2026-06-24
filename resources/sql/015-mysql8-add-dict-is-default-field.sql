-- 为数据字典增加默认数据标识字段。
-- 执行方式示例：mysql -u root -p < resources/sql/015-mysql8-add-dict-is-default-field.sql
-- 说明：脚本可重复执行，会给字典类型和字典数据补充 is_default 字段，并同步标记平台初始化字典为系统默认数据。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE hospital_system;

DROP PROCEDURE IF EXISTS migrate_dict_is_default_field;

DELIMITER $$
CREATE PROCEDURE migrate_dict_is_default_field()
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'sys_dict_type'
          AND column_name = 'is_default'
    ) THEN
        ALTER TABLE `sys_dict_type`
            ADD COLUMN `is_default` tinyint NOT NULL DEFAULT 1 COMMENT '是否默认数据（0=系统默认不可删除，1=普通数据可删除）' AFTER `remark`;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'sys_dict_data'
          AND column_name = 'is_default'
    ) THEN
        ALTER TABLE `sys_dict_data`
            ADD COLUMN `is_default` tinyint NOT NULL DEFAULT 1 COMMENT '是否默认数据（0=系统默认不可删除，1=普通数据可删除）' AFTER `remark`;
    END IF;

    UPDATE `sys_dict_type`
    SET `is_default` = 0,
        `update_time` = NOW()
    WHERE `tenant_id` = '0'
      AND `deleted` = 0;

    UPDATE `sys_dict_data`
    SET `is_default` = 0,
        `update_time` = NOW()
    WHERE `tenant_id` = '0'
      AND `deleted` = 0;

    INSERT INTO `sys_dict_type` (`tenant_id`, `dict_name`, `dict_type`, `remark`, `is_default`, `create_time`, `update_time`, `deleted`)
    SELECT tenant.`tenant_id`, template.`dict_name`, template.`dict_type`, template.`remark`, 0, NOW(), NOW(), 0
    FROM `sys_tenant` tenant
    JOIN `sys_dict_type` template
      ON template.`tenant_id` = '0'
     AND template.`is_default` = 0
     AND template.`deleted` = 0
    WHERE tenant.`tenant_id` <> '0'
      AND tenant.`deleted` = 0
    ON DUPLICATE KEY UPDATE
        `dict_name` = VALUES(`dict_name`),
        `remark` = VALUES(`remark`),
        `is_default` = 0,
        `deleted` = 0,
        `update_time` = NOW();

    INSERT INTO `sys_dict_data` (`tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `remark`, `is_default`, `create_time`, `update_time`, `deleted`)
    SELECT tenant.`tenant_id`, template.`dict_sort`, template.`dict_label`, template.`dict_value`, template.`dict_type`, template.`remark`, 0, NOW(), NOW(), 0
    FROM `sys_tenant` tenant
    JOIN `sys_dict_data` template
      ON template.`tenant_id` = '0'
     AND template.`is_default` = 0
     AND template.`deleted` = 0
    WHERE tenant.`tenant_id` <> '0'
      AND tenant.`deleted` = 0
    ON DUPLICATE KEY UPDATE
        `dict_sort` = VALUES(`dict_sort`),
        `dict_label` = VALUES(`dict_label`),
        `remark` = VALUES(`remark`),
        `is_default` = 0,
        `deleted` = 0,
        `update_time` = NOW();
END$$
DELIMITER ;

CALL migrate_dict_is_default_field();

DROP PROCEDURE IF EXISTS migrate_dict_is_default_field;

SET FOREIGN_KEY_CHECKS = 1;

-- 菜单表默认数据保护字段迁移脚本。
-- 执行方式示例：mysql -u root -p < resources/sql/006-mysql8-add-menu-is-default-field.sql

USE hospital_system;

DROP PROCEDURE IF EXISTS migrate_menu_is_default_field;

DELIMITER $$
CREATE PROCEDURE migrate_menu_is_default_field()
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'sys_menu'
          AND column_name = 'is_default'
    ) THEN
        ALTER TABLE `sys_menu`
            ADD COLUMN `is_default` tinyint NOT NULL DEFAULT 1 COMMENT '是否默认数据（0=系统默认不可删除，1=普通数据可删除）' AFTER `status`;
    END IF;

    UPDATE `sys_menu` SET `is_default` = 0 WHERE `tenant_id` = '0';
END$$
DELIMITER ;

CALL migrate_menu_is_default_field();

DROP PROCEDURE IF EXISTS migrate_menu_is_default_field;

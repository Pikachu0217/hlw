-- 菜单表多租户套餐初始化迁移脚本。
-- 执行方式示例：mysql -u root -p < resources/sql/003-mysql8-menu-tenant-package-bootstrap.sql

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE hospital_system;

DROP PROCEDURE IF EXISTS migrate_menu_tenant_package_bootstrap;

DELIMITER $$
CREATE PROCEDURE migrate_menu_tenant_package_bootstrap()
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND table_name = 'sys_menu'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND table_name = 'menu'
    ) THEN
        RENAME TABLE `sys_menu` TO `menu`;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND table_name = 'menu'
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'menu表不存在，请先创建或从sys_menu迁移';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'menu'
          AND column_name = 'source_menu_id'
    ) THEN
        ALTER TABLE `menu`
            ADD COLUMN `source_menu_id` bigint DEFAULT NULL COMMENT '平台模板菜单编号' AFTER `icon`;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'menu'
          AND index_name = 'idx_menu_tenant_parent'
    ) THEN
        ALTER TABLE `menu`
            ADD KEY `idx_menu_tenant_parent` (`tenant_id`, `parent_id`);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'menu'
          AND index_name = 'idx_menu_tenant_status'
    ) THEN
        ALTER TABLE `menu`
            ADD KEY `idx_menu_tenant_status` (`tenant_id`, `status`);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'menu'
          AND index_name = 'uk_menu_tenant_source'
    ) THEN
        ALTER TABLE `menu`
            ADD UNIQUE KEY `uk_menu_tenant_source` (`tenant_id`, `source_menu_id`);
    END IF;

    ALTER TABLE `menu` COMMENT = '菜单权限表';

    DELETE package_menu
    FROM `sys_tenant_package_menu` package_menu
    LEFT JOIN `menu` template_menu
        ON template_menu.`tenant_id` = package_menu.`tenant_id`
       AND template_menu.`id` = package_menu.`menu_id`
    WHERE package_menu.`tenant_id` = '0'
      AND template_menu.`id` IS NULL;
END$$
DELIMITER ;

CALL migrate_menu_tenant_package_bootstrap();

DROP PROCEDURE IF EXISTS migrate_menu_tenant_package_bootstrap;

SET FOREIGN_KEY_CHECKS = 1;

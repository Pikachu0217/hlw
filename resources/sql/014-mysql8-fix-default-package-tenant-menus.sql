-- 修复默认套餐租户菜单授权不完整问题。
-- 执行方式示例：mysql -u root -p < resources/sql/014-mysql8-fix-default-package-tenant-menus.sql
-- 说明：脚本可重复执行，会补齐默认套餐绑定、既有默认套餐租户菜单和租户管理员菜单授权。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE hospital_system;

DROP PROCEDURE IF EXISTS fix_default_package_tenant_menus;

DELIMITER $$
CREATE PROCEDURE fix_default_package_tenant_menus()
BEGIN
    DECLARE default_package_id BIGINT DEFAULT NULL;

    SELECT `id` INTO default_package_id
    FROM `sys_tenant_package`
    WHERE `tenant_id` = '0'
      AND `package_name` = '默认套餐'
      AND `deleted` = 0
    ORDER BY `id`
    LIMIT 1;

    IF default_package_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '默认套餐不存在，无法修复租户菜单授权';
    END IF;

    INSERT INTO `sys_tenant_package_menu` (`tenant_id`, `package_id`, `menu_id`, `create_time`, `update_time`, `deleted`)
    SELECT '0', default_package_id, menu.`id`, NOW(), NOW(), 0
    FROM `sys_menu` menu
    WHERE menu.`tenant_id` = '0'
      AND menu.`deleted` = 0
    ON DUPLICATE KEY UPDATE
        `deleted` = 0,
        `update_time` = NOW();

    INSERT INTO `sys_menu` (`tenant_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `menu_type`, `visible`, `status`, `is_default`, `perms`, `icon`, `source_menu_id`, `remark`, `create_time`, `update_time`, `deleted`)
    SELECT tenant.`tenant_id`, template.`menu_name`, 0, template.`order_num`, template.`path`, template.`component`, template.`is_frame`, template.`menu_type`, template.`visible`, template.`status`, 0, template.`perms`, template.`icon`, template.`id`, template.`remark`, NOW(), NOW(), 0
    FROM `sys_tenant` tenant
    JOIN `sys_menu` template
      ON template.`tenant_id` = '0'
     AND template.`deleted` = 0
    WHERE tenant.`tenant_id` <> '0'
      AND tenant.`package_id` = default_package_id
      AND tenant.`deleted` = 0
    ON DUPLICATE KEY UPDATE
        `menu_name` = VALUES(`menu_name`),
        `order_num` = VALUES(`order_num`),
        `path` = VALUES(`path`),
        `component` = VALUES(`component`),
        `is_frame` = VALUES(`is_frame`),
        `menu_type` = VALUES(`menu_type`),
        `visible` = VALUES(`visible`),
        `status` = VALUES(`status`),
        `is_default` = 0,
        `perms` = VALUES(`perms`),
        `icon` = VALUES(`icon`),
        `remark` = VALUES(`remark`),
        `deleted` = 0,
        `update_time` = NOW();

    UPDATE `sys_menu` child
    JOIN `sys_tenant` tenant
      ON tenant.`tenant_id` = child.`tenant_id`
     AND tenant.`package_id` = default_package_id
     AND tenant.`deleted` = 0
    JOIN `sys_menu` template
      ON template.`tenant_id` = '0'
     AND template.`id` = child.`source_menu_id`
     AND template.`deleted` = 0
    LEFT JOIN `sys_menu` parent
      ON parent.`tenant_id` = child.`tenant_id`
     AND parent.`source_menu_id` = template.`parent_id`
     AND parent.`deleted` = 0
    SET child.`parent_id` = CASE
            WHEN template.`parent_id` = 0 THEN 0
            ELSE COALESCE(parent.`id`, 0)
        END,
        child.`update_time` = NOW()
    WHERE child.`tenant_id` <> '0'
      AND child.`deleted` = 0;

    INSERT INTO `sys_role_menu` (`tenant_id`, `role_id`, `menu_id`, `create_time`, `update_time`, `deleted`)
    SELECT tenant.`tenant_id`, role.`id`, menu.`id`, NOW(), NOW(), 0
    FROM `sys_tenant` tenant
    JOIN `sys_role` role
      ON role.`tenant_id` = tenant.`tenant_id`
     AND role.`role_code` = 'tenant_admin'
     AND role.`deleted` = 0
    JOIN `sys_menu` menu
      ON menu.`tenant_id` = tenant.`tenant_id`
     AND menu.`deleted` = 0
    WHERE tenant.`tenant_id` <> '0'
      AND tenant.`package_id` = default_package_id
      AND tenant.`deleted` = 0
    ON DUPLICATE KEY UPDATE
        `deleted` = 0,
        `update_time` = NOW();
END$$
DELIMITER ;

CALL fix_default_package_tenant_menus();

DROP PROCEDURE IF EXISTS fix_default_package_tenant_menus;

SET FOREIGN_KEY_CHECKS = 1;

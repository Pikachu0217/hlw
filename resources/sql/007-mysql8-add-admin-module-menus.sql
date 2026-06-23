-- 管理端必要业务模块菜单补齐脚本。
-- 执行方式示例：mysql -u root -p < resources/sql/007-mysql8-add-admin-module-menus.sql
-- 说明：脚本可重复执行，按平台租户 tenant_id=0 补齐菜单，并同步绑定系统管理员角色和默认套餐。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE hospital_system;

DROP PROCEDURE IF EXISTS migrate_admin_module_menus;

DELIMITER $$
CREATE PROCEDURE migrate_admin_module_menus()
BEGIN
    DECLARE admin_role_id BIGINT DEFAULT NULL;
    DECLARE default_package_id BIGINT DEFAULT NULL;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND table_name = 'sys_menu'
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'sys_menu表不存在，请先创建系统菜单表';
    END IF;

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

    DROP TEMPORARY TABLE IF EXISTS `tmp_admin_module_menu`;
    CREATE TEMPORARY TABLE `tmp_admin_module_menu` (
        `menu_code` varchar(64) NOT NULL COMMENT '脚本内部菜单编码',
        `parent_code` varchar(64) DEFAULT NULL COMMENT '脚本内部父级菜单编码',
        `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
        `order_num` int NOT NULL COMMENT '显示顺序',
        `path` varchar(200) NOT NULL COMMENT '路由地址',
        `component` varchar(255) DEFAULT NULL COMMENT '组件路径',
        `menu_type` char(1) NOT NULL COMMENT '菜单类型（M目录 C菜单 F按钮）',
        `perms` varchar(100) DEFAULT NULL COMMENT '权限标识',
        `icon` varchar(100) NOT NULL COMMENT '菜单图标',
        `remark` varchar(500) DEFAULT '' COMMENT '备注',
        PRIMARY KEY (`menu_code`)
    ) ENGINE=Memory DEFAULT CHARSET=utf8mb4 COMMENT='管理端菜单临时表';

    INSERT INTO `tmp_admin_module_menu` (`menu_code`, `parent_code`, `menu_name`, `order_num`, `path`, `component`, `menu_type`, `perms`, `icon`, `remark`) VALUES
        ('dashboard', NULL, '工作台', 1, '/dashboard', 'dashboard/index', 'C', 'dashboard:view', 'dashboard', '工作台菜单'),
        ('tenant', NULL, '租户管理', 2, '/tenant', 'tenant/index', 'C', 'tenant:list', 'tenant', '租户管理菜单'),
        ('system', NULL, '系统管理', 3, '', '', 'M', 'system:index', 'system', '系统管理目录'),
        ('system-user', 'system', '用户管理', 1, '/system/user', 'system/user/index', 'C', 'system:user:index', 'user', '用户管理菜单'),
        ('system-role', 'system', '角色管理', 2, '/system/role', 'system/role/index', 'C', 'system:role:index', 'role', '角色管理菜单'),
        ('system-menu', 'system', '菜单管理', 3, '/system/menu', 'system/menu/index', 'C', 'system:menu:index', 'menu', '菜单管理菜单'),
        ('system-dict', 'system', '字典管理', 4, '/system/dict', 'system/dict/index', 'C', 'system:dict:index', 'dict', '字典管理菜单'),
        ('system-config', 'system', '参数配置', 5, '/system/config', 'system/config/index', 'C', 'system:config:index', 'tree-table', '参数配置菜单'),
        ('system-post', 'system', '岗位管理', 6, '/system/post', 'system/post/index', 'C', 'system:post:index', 'post', '岗位管理菜单'),
        ('system-dept', 'system', '部门管理', 7, '/system/dept', 'system/dept/index', 'C', 'system:dept:index', 'dept', '部门管理菜单'),
        ('system-tenant-package', 'system', '套餐管理', 8, '/system/tenant-package', 'system/tenant-package/index', 'C', 'system:tenant-package:index', 'post', '套餐管理菜单'),
        ('system-notice', 'system', '通知公告', 9, '/system/notice', 'system/notice/index', 'C', 'system:notice:index', 'notice', '通知公告菜单'),
        ('system-logs', 'system', '系统日志', 10, '/system/logs', 'system/logs/index', 'C', 'system:logs:index', 'log', '系统日志菜单'),
        ('doctor', NULL, '医生管理', 4, '', '', 'M', 'doctor:index', 'doctor', '医生管理目录'),
        ('doctor-list', 'doctor', '医生名录', 1, '/doctor', 'doctor/index', 'C', 'doctor:list', 'doctor', '医生名录菜单'),
        ('doctor-departments', 'doctor', '科室管理', 2, '/doctor/departments', 'doctor/departments/index', 'C', 'doctor:department:index', 'dept', '科室管理菜单'),
        ('patient', NULL, '患者管理', 5, '/patient', 'patient/index', 'C', 'patient:index', 'patient', '患者管理菜单'),
        ('consult', NULL, '咨询单', 6, '/consult', 'consult/index', 'C', 'consult:index', 'consult', '咨询单管理菜单'),
        ('appointment', NULL, '预约管理', 7, '/appointment', 'appointment/index', 'C', 'appointment:index', 'appointment', '预约管理菜单'),
        ('prescription', NULL, '处方中心', 8, '/prescription', 'prescription/index', 'C', 'prescription:index', 'prescription', '处方中心菜单'),
        ('drug', NULL, '药品目录', 9, '/drug', 'drug/index', 'C', 'drug:index', 'drug', '药品目录菜单'),
        ('order', NULL, '订单中心', 10, '/order', 'order/index', 'C', 'order:index', 'order', '订单中心菜单')
    ON DUPLICATE KEY UPDATE
        `parent_code` = VALUES(`parent_code`),
        `menu_name` = VALUES(`menu_name`),
        `order_num` = VALUES(`order_num`),
        `path` = VALUES(`path`),
        `component` = VALUES(`component`),
        `menu_type` = VALUES(`menu_type`),
        `perms` = VALUES(`perms`),
        `icon` = VALUES(`icon`),
        `remark` = VALUES(`remark`);

    INSERT INTO `sys_menu` (`tenant_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `menu_type`, `visible`, `status`, `is_default`, `perms`, `icon`, `remark`, `create_time`, `update_time`, `deleted`)
    SELECT '0', seed.`menu_name`, 0, seed.`order_num`, seed.`path`, seed.`component`, 1, seed.`menu_type`, '0', '0', 0, seed.`perms`, seed.`icon`, seed.`remark`, NOW(), NOW(), 0
    FROM `tmp_admin_module_menu` seed
    LEFT JOIN `sys_menu` menu
        ON menu.`tenant_id` = '0'
       AND menu.`deleted` = 0
       AND (
            (seed.`perms` <> '' AND menu.`perms` = seed.`perms`)
            OR (seed.`path` <> '' AND menu.`path` = seed.`path`)
            OR (seed.`parent_code` IS NULL AND seed.`path` = '' AND menu.`menu_name` = seed.`menu_name`)
       )
    WHERE menu.`id` IS NULL;

    UPDATE `sys_menu` menu
    JOIN `tmp_admin_module_menu` seed
      ON menu.`tenant_id` = '0'
     AND menu.`deleted` = 0
     AND (
          (seed.`perms` <> '' AND menu.`perms` = seed.`perms`)
          OR (seed.`path` <> '' AND menu.`path` = seed.`path`)
          OR (seed.`parent_code` IS NULL AND seed.`path` = '' AND menu.`menu_name` = seed.`menu_name`)
     )
    SET menu.`menu_name` = seed.`menu_name`,
        menu.`order_num` = seed.`order_num`,
        menu.`path` = seed.`path`,
        menu.`component` = seed.`component`,
        menu.`is_frame` = 1,
        menu.`menu_type` = seed.`menu_type`,
        menu.`visible` = '0',
        menu.`status` = '0',
        menu.`is_default` = 0,
        menu.`perms` = seed.`perms`,
        menu.`icon` = seed.`icon`,
        menu.`remark` = seed.`remark`,
        menu.`update_time` = NOW();

    UPDATE `sys_menu` child
    JOIN `tmp_admin_module_menu` child_seed
      ON child.`tenant_id` = '0'
     AND child.`deleted` = 0
     AND (
          (child_seed.`perms` <> '' AND child.`perms` = child_seed.`perms`)
          OR (child_seed.`path` <> '' AND child.`path` = child_seed.`path`)
     )
    JOIN `tmp_admin_module_menu` parent_seed
      ON parent_seed.`menu_code` = child_seed.`parent_code`
    JOIN `sys_menu` parent
      ON parent.`tenant_id` = '0'
     AND parent.`deleted` = 0
     AND (
          (parent_seed.`perms` <> '' AND parent.`perms` = parent_seed.`perms`)
          OR (parent_seed.`path` <> '' AND parent.`path` = parent_seed.`path`)
          OR (parent_seed.`parent_code` IS NULL AND parent_seed.`path` = '' AND parent.`menu_name` = parent_seed.`menu_name`)
     )
    SET child.`parent_id` = parent.`id`,
        child.`update_time` = NOW();

    UPDATE `sys_menu` root
    JOIN `tmp_admin_module_menu` seed
      ON root.`tenant_id` = '0'
     AND root.`deleted` = 0
     AND seed.`parent_code` IS NULL
     AND (
          (seed.`perms` <> '' AND root.`perms` = seed.`perms`)
          OR (seed.`path` <> '' AND root.`path` = seed.`path`)
          OR (seed.`path` = '' AND root.`menu_name` = seed.`menu_name`)
     )
    SET root.`parent_id` = 0,
        root.`update_time` = NOW();

    SELECT `id` INTO admin_role_id
    FROM `sys_role`
    WHERE `tenant_id` = '0'
      AND `role_code` = 'SYSTEM_ADMIN'
      AND `deleted` = 0
    ORDER BY `id`
    LIMIT 1;

    IF admin_role_id IS NOT NULL THEN
        INSERT INTO `sys_role_menu` (`tenant_id`, `role_id`, `menu_id`, `create_time`, `update_time`, `deleted`)
        SELECT '0', admin_role_id, menu.`id`, NOW(), NOW(), 0
        FROM `sys_menu` menu
        LEFT JOIN `sys_role_menu` relation
          ON relation.`tenant_id` = '0'
         AND relation.`role_id` = admin_role_id
         AND relation.`menu_id` = menu.`id`
         AND relation.`deleted` = 0
        WHERE menu.`tenant_id` = '0'
          AND menu.`deleted` = 0
          AND relation.`id` IS NULL
        ON DUPLICATE KEY UPDATE
            `deleted` = 0,
            `update_time` = NOW();
    END IF;

    SELECT `id` INTO default_package_id
    FROM `sys_tenant_package`
    WHERE `tenant_id` = '0'
      AND `package_name` = '默认套餐'
      AND `deleted` = 0
    ORDER BY `id`
    LIMIT 1;

    IF default_package_id IS NOT NULL THEN
        INSERT INTO `sys_tenant_package_menu` (`tenant_id`, `package_id`, `menu_id`, `create_time`, `update_time`, `deleted`)
        SELECT '0', default_package_id, menu.`id`, NOW(), NOW(), 0
        FROM `sys_menu` menu
        LEFT JOIN `sys_tenant_package_menu` relation
          ON relation.`tenant_id` = '0'
         AND relation.`package_id` = default_package_id
         AND relation.`menu_id` = menu.`id`
         AND relation.`deleted` = 0
        WHERE menu.`tenant_id` = '0'
          AND menu.`deleted` = 0
          AND relation.`id` IS NULL
        ON DUPLICATE KEY UPDATE
            `deleted` = 0,
            `update_time` = NOW();
    END IF;

    DROP TEMPORARY TABLE IF EXISTS `tmp_admin_module_menu`;
END$$
DELIMITER ;

CALL migrate_admin_module_menus();

DROP PROCEDURE IF EXISTS migrate_admin_module_menus;

SET FOREIGN_KEY_CHECKS = 1;

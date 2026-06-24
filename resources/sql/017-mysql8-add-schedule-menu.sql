-- =============================================================
-- 017 — sys_menu 新增排班管理菜单
-- 在医疗资源目录下插入排班管理（order=2），科室管理顺延为 order=3
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `hospital_system`;

-- ----------------------------
-- 1. 模板菜单（tenant_id = '0'）
-- ----------------------------
-- 排班管理：order=2，科室管理从 2 改为 3
UPDATE `sys_menu` SET `order_num` = 3 WHERE `id` = 44 AND `tenant_id` = '0';

INSERT IGNORE INTO `sys_menu` (`id`, `tenant_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `menu_type`, `visible`, `status`, `is_default`, `perms`, `icon`, `source_menu_id`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
VALUES (146, '0', '排班管理', 57, 2, '/doctor/schedule', 'doctor/schedule/index', 1, 'C', '0', '0', 0, 'doctor:schedule:index', 'schedule', NULL, '排班管理菜单', NULL, NOW(), NULL, NOW(), 0);

-- ----------------------------
-- 2. 租户菜单副本（tenant-specific）
-- ----------------------------
-- 租户 17822840538455380：医疗资源 parent_id=89
UPDATE `sys_menu` SET `order_num` = 3 WHERE `id` = 82 AND `tenant_id` = '17822840538455380';
INSERT IGNORE INTO `sys_menu` (`id`, `tenant_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `menu_type`, `visible`, `status`, `is_default`, `perms`, `icon`, `source_menu_id`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
VALUES (147, '17822840538455380', '排班管理', 89, 2, '/doctor/schedule', 'doctor/schedule/index', 1, 'C', '0', '0', 0, 'doctor:schedule:index', 'schedule', 146, '排班管理菜单', NULL, NOW(), NULL, NOW(), 0);

-- 租户 17822845273849308：医疗资源 parent_id=102
UPDATE `sys_menu` SET `order_num` = 3 WHERE `id` = 115 AND `tenant_id` = '17822845273849308';
INSERT IGNORE INTO `sys_menu` (`id`, `tenant_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `menu_type`, `visible`, `status`, `is_default`, `perms`, `icon`, `source_menu_id`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
VALUES (148, '17822845273849308', '排班管理', 102, 2, '/doctor/schedule', 'doctor/schedule/index', 1, 'C', '0', '0', 0, 'doctor:schedule:index', 'schedule', 146, '排班管理菜单', NULL, NOW(), NULL, NOW(), 0);

-- 租户 17822850632175495：医疗资源 parent_id=124（当前 ID 最大到 145，此处用 149）
UPDATE `sys_menu` SET `order_num` = 3 WHERE `id` = 137 AND `tenant_id` = '17822850632175495';
INSERT IGNORE INTO `sys_menu` (`id`, `tenant_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `menu_type`, `visible`, `status`, `is_default`, `perms`, `icon`, `source_menu_id`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`)
VALUES (149, '17822850632175495', '排班管理', 124, 2, '/doctor/schedule', 'doctor/schedule/index', 1, 'C', '0', '0', 0, 'doctor:schedule:index', 'schedule', 146, '排班管理菜单', NULL, NOW(), NULL, NOW(), 0);

SET FOREIGN_KEY_CHECKS = 1;

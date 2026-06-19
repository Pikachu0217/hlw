-- system 模块统一 BaseEntity 公共字段迁移脚本。
-- 执行方式示例：mysql -u root -p < resources/sql/002-mysql8-system-base-entity.sql

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE hospital_system;

ALTER TABLE `sys_config` DROP COLUMN `create_dept`;
ALTER TABLE `sys_dept` DROP COLUMN `create_dept`;
ALTER TABLE `sys_dict_data` DROP COLUMN `create_dept`;
ALTER TABLE `sys_dict_type` DROP COLUMN `create_dept`;
ALTER TABLE `sys_menu` DROP COLUMN `create_dept`;
ALTER TABLE `sys_notice` DROP COLUMN `create_dept`;
ALTER TABLE `sys_post` DROP COLUMN `create_dept`;
ALTER TABLE `sys_role` DROP COLUMN `create_dept`;
ALTER TABLE `sys_tenant` DROP COLUMN `create_dept`;
ALTER TABLE `sys_tenant_package` DROP COLUMN `create_dept`;
ALTER TABLE `sys_user` DROP COLUMN `create_dept`;

ALTER TABLE `sys_menu`
    ADD COLUMN `tenant_id` varchar(32) NOT NULL DEFAULT '0' COMMENT '租户编号' AFTER `id`;

ALTER TABLE `sys_tenant_package`
    ADD COLUMN `tenant_id` varchar(32) NOT NULL DEFAULT '0' COMMENT '租户编号' AFTER `id`;

ALTER TABLE `sys_login_info`
    ADD COLUMN `create_time` datetime DEFAULT NULL COMMENT '创建时间' AFTER `login_time`,
    ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间' AFTER `create_time`,
    ADD COLUMN `create_by` varchar(34) DEFAULT NULL COMMENT '创建者用户ID' AFTER `update_time`,
    ADD COLUMN `update_by` varchar(34) DEFAULT NULL COMMENT '更新者用户ID' AFTER `create_by`,
    ADD COLUMN `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）' AFTER `update_by`;

ALTER TABLE `sys_operator_log`
    ADD COLUMN `create_time` datetime DEFAULT NULL COMMENT '创建时间' AFTER `cost_time`,
    ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间' AFTER `create_time`,
    ADD COLUMN `create_by` varchar(34) DEFAULT NULL COMMENT '创建者用户ID' AFTER `update_time`,
    ADD COLUMN `update_by` varchar(34) DEFAULT NULL COMMENT '更新者用户ID' AFTER `create_by`,
    ADD COLUMN `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）' AFTER `update_by`;

ALTER TABLE `sys_role_dept`
    ADD COLUMN `create_time` datetime DEFAULT NULL COMMENT '创建时间' AFTER `dept_id`,
    ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间' AFTER `create_time`,
    ADD COLUMN `create_by` varchar(34) DEFAULT NULL COMMENT '创建者用户ID' AFTER `update_time`,
    ADD COLUMN `update_by` varchar(34) DEFAULT NULL COMMENT '更新者用户ID' AFTER `create_by`,
    ADD COLUMN `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）' AFTER `update_by`;

ALTER TABLE `sys_role_menu`
    ADD COLUMN `create_time` datetime DEFAULT NULL COMMENT '创建时间' AFTER `menu_id`,
    ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间' AFTER `create_time`,
    ADD COLUMN `create_by` varchar(34) DEFAULT NULL COMMENT '创建者用户ID' AFTER `update_time`,
    ADD COLUMN `update_by` varchar(34) DEFAULT NULL COMMENT '更新者用户ID' AFTER `create_by`,
    ADD COLUMN `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）' AFTER `update_by`;

ALTER TABLE `sys_tenant_package_menu`
    ADD COLUMN `tenant_id` varchar(32) NOT NULL DEFAULT '0' COMMENT '租户编号' AFTER `id`,
    ADD COLUMN `create_time` datetime DEFAULT NULL COMMENT '创建时间' AFTER `menu_id`,
    ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间' AFTER `create_time`,
    ADD COLUMN `create_by` varchar(34) DEFAULT NULL COMMENT '创建者用户ID' AFTER `update_time`,
    ADD COLUMN `update_by` varchar(34) DEFAULT NULL COMMENT '更新者用户ID' AFTER `create_by`,
    ADD COLUMN `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）' AFTER `update_by`;

ALTER TABLE `sys_user_post`
    ADD COLUMN `create_time` datetime DEFAULT NULL COMMENT '创建时间' AFTER `post_id`,
    ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间' AFTER `create_time`,
    ADD COLUMN `create_by` varchar(34) DEFAULT NULL COMMENT '创建者用户ID' AFTER `update_time`,
    ADD COLUMN `update_by` varchar(34) DEFAULT NULL COMMENT '更新者用户ID' AFTER `create_by`,
    ADD COLUMN `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）' AFTER `update_by`;

ALTER TABLE `sys_user_role`
    ADD COLUMN `create_time` datetime DEFAULT NULL COMMENT '创建时间' AFTER `role_id`,
    ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间' AFTER `create_time`,
    ADD COLUMN `create_by` varchar(34) DEFAULT NULL COMMENT '创建者用户ID' AFTER `update_time`,
    ADD COLUMN `update_by` varchar(34) DEFAULT NULL COMMENT '更新者用户ID' AFTER `create_by`,
    ADD COLUMN `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）' AFTER `update_by`;

ALTER TABLE `sys_tenant_package_menu` DROP INDEX `uk_sys_tenant_package_menu`;
ALTER TABLE `sys_tenant_package_menu` ADD UNIQUE KEY `uk_sys_tenant_package_menu` (`tenant_id`, `package_id`, `menu_id`);

SET FOREIGN_KEY_CHECKS = 1;

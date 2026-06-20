-- 添加 is_default 字段来标记系统默认数据，防止误删或修改
-- 0=默认数据，不可删除或修改；1=非默认数据，可以删除或修改

USE hospital_system;

-- 为 sys_role 表添加 is_default 字段
ALTER TABLE `sys_role` ADD COLUMN `is_default` tinyint NOT NULL DEFAULT 1 COMMENT '是否默认数据（0=系统默认不可删除，1=普通数据可删除）' AFTER `status`;

-- 为 sys_dept 表添加 is_default 字段
ALTER TABLE `sys_dept` ADD COLUMN `is_default` tinyint NOT NULL DEFAULT 1 COMMENT '是否默认数据（0=系统默认不可删除，1=普通数据可删除）' AFTER `status`;

-- 为 sys_post 表添加 is_default 字段
ALTER TABLE `sys_post` ADD COLUMN `is_default` tinyint NOT NULL DEFAULT 1 COMMENT '是否默认数据（0=系统默认不可删除，1=普通数据可删除）' AFTER `status`;

-- 为 sys_tenant_package 表添加 is_default 字段
ALTER TABLE `sys_tenant_package` ADD COLUMN `is_default` tinyint NOT NULL DEFAULT 1 COMMENT '是否默认数据（0=系统默认不可删除，1=普通数据可删除）' AFTER `status`;

-- 为 sys_tenant 表添加 is_default 字段
ALTER TABLE `sys_tenant` ADD COLUMN `is_default` tinyint NOT NULL DEFAULT 1 COMMENT '是否默认数据（0=系统默认不可删除，1=普通数据可删除）' AFTER `status`;

-- 为 sys_user 表添加 is_default 字段
ALTER TABLE `sys_user` ADD COLUMN `is_default` tinyint NOT NULL DEFAULT 1 COMMENT '是否默认数据（0=系统默认不可删除，1=普通数据可删除）' AFTER `status`;

-- 更新现有的系统默认数据
-- 标记 SYSTEM_ADMIN 角色为默认（tenant_id=0, role_code=SYSTEM_ADMIN）
UPDATE `sys_role` SET `is_default` = 0 WHERE `tenant_id` = '0' AND `role_code` = 'SYSTEM_ADMIN';

-- 标记运营中心部门为默认（tenant_id=0, dept_name=运营中心）
UPDATE `sys_dept` SET `is_default` = 0 WHERE `tenant_id` = '0' AND `dept_name` = '运营中心';

-- 标记运营管理员岗位为默认（tenant_id=0, post_code=OPERATIONS_ADMIN）
UPDATE `sys_post` SET `is_default` = 0 WHERE `tenant_id` = '0' AND `post_code` = 'OPERATIONS_ADMIN';

-- 标记默认套餐为默认（package_name=默认套餐）
UPDATE `sys_tenant_package` SET `is_default` = 0 WHERE `package_name` = '默认套餐';

-- 标记平台租户为默认（tenant_id=0）
UPDATE `sys_tenant` SET `is_default` = 0 WHERE `tenant_id` = '0';

-- 标记 hlw_admin 用户为默认（user_name=hlw_admin）
UPDATE `sys_user` SET `is_default` = 0 WHERE `user_name` = 'hlw_admin';

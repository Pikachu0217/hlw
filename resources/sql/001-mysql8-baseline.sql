-- 互联网医院 MySQL 8 基线初始化脚本。
-- 本文件按顺序作为 001 号 schema 执行文件维护，后续 schema 变更请新增 002、003 等顺序脚本。
-- 执行方式示例：mysql -u root -p < resources/sql/001-mysql8-baseline.sql

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS hospital_gateway DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS hospital_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS hospital_patient DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS hospital_doctor DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS hospital_consult DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS hospital_appointment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS hospital_prescription DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS hospital_drug DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS hospital_order DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE hospital_gateway;

CREATE TABLE IF NOT EXISTS gw_route_config (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号' PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '平台租户编号',
    route_code VARCHAR(64) NOT NULL COMMENT '路由编码',
    uri VARCHAR(256) NOT NULL COMMENT '服务地址',
    path_predicate VARCHAR(256) NOT NULL COMMENT '路径断言',
    sort INT NOT NULL DEFAULT 0 COMMENT '显示排序',
    status VARCHAR(32) NOT NULL DEFAULT '0' COMMENT '路由状态',
    remark VARCHAR(256) NOT NULL DEFAULT '' COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人编号',
    update_by BIGINT COMMENT '更新人编号',
    deleted SMALLINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识'
) COMMENT='网关路由配置表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE UNIQUE INDEX uk_gw_route_config_route_code ON gw_route_config (route_code, deleted);
CREATE INDEX idx_gw_route_config_sort ON gw_route_config (sort, id);

INSERT INTO gw_route_config (id, tenant_id, route_code, uri, path_predicate, sort, status, remark)
VALUES
    (1, 0, 'hospital-auth', 'lb://hospital-auth', '/auth/**', 10, '0', '认证服务静态路由'),
    (2, 0, 'hospital-system', 'lb://hospital-system', '/system/**', 20, '0', '系统服务静态路由'),
    (3, 0, 'hospital-doctor', 'lb://hospital-doctor', '/doctor/**', 30, '0', '医生服务静态路由'),
    (4, 0, 'hospital-patient', 'lb://hospital-patient', '/patient/**', 40, '0', '患者服务静态路由'),
    (5, 0, 'hospital-appointment', 'lb://hospital-appointment', '/appointment/**', 50, '0', '预约服务静态路由'),
    (6, 0, 'hospital-consult', 'lb://hospital-consult', '/consult/**,/ws/consult/**', 60, '0', '问诊服务静态路由'),
    (7, 0, 'hospital-prescription', 'lb://hospital-prescription', '/prescription/**', 70, '0', '处方服务静态路由'),
    (8, 0, 'hospital-drug', 'lb://hospital-drug', '/drug/**', 80, '0', '药品服务静态路由'),
    (9, 0, 'hospital-order', 'lb://hospital-order', '/order/**', 90, '0', '订单服务静态路由')
ON DUPLICATE KEY UPDATE id = id;

CREATE TABLE IF NOT EXISTS local_message (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号' PRIMARY KEY,
    topic VARCHAR(128) NOT NULL COMMENT '消息主题',
    body TEXT NOT NULL COMMENT '消息内容',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    max_retry INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    next_retry_time DATETIME COMMENT '下次重试时间',
    -- status: PENDING, SENT, FAILED
    status VARCHAR(16) NOT NULL COMMENT '消息状态',
    error_msg TEXT COMMENT '错误信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='本地消息表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

USE hospital_system;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for local_message
-- ----------------------------
DROP TABLE IF EXISTS `local_message`;
CREATE TABLE `local_message` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键编号',
                                 `topic` varchar(128) NOT NULL COMMENT '消息主题',
                                 `body` text NOT NULL COMMENT '消息内容',
                                 `retry_count` int NOT NULL DEFAULT '0' COMMENT '重试次数',
                                 `max_retry` int NOT NULL DEFAULT '3' COMMENT '最大重试次数',
                                 `next_retry_time` datetime DEFAULT NULL COMMENT '下次重试时间',
                                 `status` varchar(16) NOT NULL COMMENT '消息状态',
                                 `error_msg` text COMMENT '错误信息',
                                 `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统服务本地消息表';

-- ----------------------------
-- Records of local_message
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '参数主键',
                              `tenant_id` varchar(32) NOT NULL DEFAULT '0' COMMENT '租户编号',
                              `config_name` varchar(100) NOT NULL DEFAULT '' COMMENT '参数名称',
                              `config_key` varchar(100) NOT NULL DEFAULT '' COMMENT '参数键名',
                              `config_value` varchar(500) NOT NULL DEFAULT '' COMMENT '参数键值',
                              `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                              `create_by` varchar(34) DEFAULT NULL COMMENT '创建者用户ID',
                              `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                              `update_by` varchar(34) DEFAULT NULL COMMENT '更新者用户ID',
                              `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                              `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_sys_config_key` (`tenant_id`,`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='参数配置表';

-- ----------------------------
-- Records of sys_config
-- ----------------------------
BEGIN;
INSERT INTO `sys_config` (`id`, `tenant_id`, `config_name`, `config_key`, `config_value`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`) VALUES (1, '0', '默认问诊时长', 'consult.default_duration_minutes', '30', '默认问诊时长', NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_config` (`id`, `tenant_id`, `config_name`, `config_key`, `config_value`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`) VALUES (2, '0', '放号提前窗口', 'appointment.release_window_minutes', '15', '放号提前窗口', NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_config` (`id`, `tenant_id`, `config_name`, `config_key`, `config_value`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`) VALUES (3, '0', '密码过期天数', 'security.password_expire_days', '90', '密码过期天数', NULL, NULL, NULL, NULL, 0);
COMMIT;

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门ID',
                            `tenant_id` varchar(32) NOT NULL DEFAULT '0' COMMENT '租户编号',
                            `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父部门ID',
                            `ancestors` varchar(500) NOT NULL DEFAULT '' COMMENT '祖级列表',
                            `dept_name` varchar(30) NOT NULL DEFAULT '' COMMENT '部门名称',
                            `order_num` int NOT NULL DEFAULT '0' COMMENT '显示顺序',
                            `leader` varchar(34) DEFAULT NULL COMMENT '负责人用户ID',
                            `phone` varchar(11) DEFAULT NULL COMMENT '联系电话',
                            `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
                            `status` tinyint NOT NULL DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
                            `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
                            `create_by` varchar(34) DEFAULT NULL COMMENT '创建者用户ID',
                            `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                            `update_by` varchar(34) DEFAULT NULL COMMENT '更新者用户ID',
                            `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_sys_dept_name` (`tenant_id`,`parent_id`,`dept_name`),
                            KEY `idx_sys_dept_parent` (`tenant_id`,`parent_id`),
                            KEY `idx_sys_dept_status` (`tenant_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
BEGIN;
INSERT INTO `sys_dept` (`id`, `tenant_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `deleted`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (1, '0', 0, '0', '运营中心', 1, 'U_550e8400e29b41d4a716446655440001', '13800001111', 'ops@hlw.local', 0, 0, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dept` (`id`, `tenant_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `deleted`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (2, '0', 1, '0,1', '药房组', 2, 'U_550e8400e29b41d4a716446655440002', '13800002222', 'pharmacy@hlw.local', 0, 0, NULL, NULL, NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典数据主键',
                                 `tenant_id` varchar(32) NOT NULL DEFAULT '0' COMMENT '租户编号',
                                 `dict_sort` int NOT NULL DEFAULT '0' COMMENT '字典排序',
                                 `dict_label` varchar(100) NOT NULL DEFAULT '' COMMENT '字典标签',
                                 `dict_value` varchar(100) NOT NULL DEFAULT '' COMMENT '字典键值',
                                 `dict_type` varchar(100) NOT NULL DEFAULT '' COMMENT '字典类型',
                                 `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                                 `create_by` varchar(34) DEFAULT NULL COMMENT '创建者用户ID',
                                 `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                 `update_by` varchar(34) DEFAULT NULL COMMENT '更新者用户ID',
                                 `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                 `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_sys_dict_data` (`tenant_id`,`dict_type`,`dict_value`),
                                 KEY `idx_sys_dict_data_type` (`tenant_id`,`dict_type`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典数据表';

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
BEGIN;
INSERT INTO `sys_dict_data` (`id`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`) VALUES (1, '0', 1, '启用', '0', 'account_status', '后台账号可登录', NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_dict_data` (`id`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`) VALUES (2, '0', 2, '停用', '1', 'account_status', '后台账号禁止登录', NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_dict_data` (`id`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`) VALUES (3, '0', 1, '目录', 'M', 'menu_type', '菜单目录节点', NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_dict_data` (`id`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`) VALUES (4, '0', 2, '菜单', 'C', 'menu_type', '可访问页面菜单', NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_dict_data` (`id`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`) VALUES (5, '0', 3, '按钮', 'F', 'menu_type', '页面按钮权限', NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_dict_data` (`id`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`) VALUES (6, '0', 1, '系统用户', 'sys_user', 'user_type', '后台系统用户', NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_dict_data` (`id`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`) VALUES (7, '0', 2, '医生', 'doctor', 'user_type', '医生工作台用户', NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_dict_data` (`id`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`) VALUES (8, '0', 3, '药师', 'pharmacist', 'user_type', '药师工作台用户', NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_dict_data` (`id`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`) VALUES (9, '0', 4, '患者', 'patient', 'user_type', '患者端用户', NULL, NULL, NULL, NULL, 0);
COMMIT;

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典主键',
                                 `tenant_id` varchar(32) NOT NULL DEFAULT '0' COMMENT '租户编号',
                                 `dict_name` varchar(100) NOT NULL DEFAULT '' COMMENT '字典名称',
                                 `dict_type` varchar(100) NOT NULL DEFAULT '' COMMENT '字典类型',
                                 `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                                 `create_by` varchar(34) DEFAULT NULL COMMENT '创建者用户ID',
                                 `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                 `update_by` varchar(34) DEFAULT NULL COMMENT '更新者用户ID',
                                 `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                 `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_sys_dict_type` (`tenant_id`,`dict_type`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典类型表';

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
BEGIN;
INSERT INTO `sys_dict_type` (`id`, `tenant_id`, `dict_name`, `dict_type`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`) VALUES (1, '0', '账号状态', 'account_status', '后台账号状态', NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_dict_type` (`id`, `tenant_id`, `dict_name`, `dict_type`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`) VALUES (2, '0', '菜单类型', 'menu_type', '系统菜单类型', NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_dict_type` (`id`, `tenant_id`, `dict_name`, `dict_type`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`) VALUES (3, '0', '用户类型', 'user_type', '后台账号用户类型', NULL, NULL, NULL, NULL, 0);
COMMIT;

-- ----------------------------
-- Table structure for sys_login_info
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_info`;
CREATE TABLE `sys_login_info` (
                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '访问ID',
                                  `tenant_id` varchar(32) NOT NULL DEFAULT '0' COMMENT '租户编号',
                                  `user_name` varchar(50) NOT NULL DEFAULT '' COMMENT '用户账号',
                                  `client_key` varchar(32) NOT NULL DEFAULT '' COMMENT '客户端',
                                  `device_type` varchar(32) NOT NULL DEFAULT '' COMMENT '设备类型',
                                  `ipaddr` varchar(128) NOT NULL DEFAULT '' COMMENT '登录IP地址',
                                  `login_location` varchar(255) DEFAULT '' COMMENT '登录地点',
                                  `browser` varchar(50) DEFAULT '' COMMENT '浏览器类型',
                                  `os` varchar(50) DEFAULT '' COMMENT '操作系统',
                                  `status` tinyint NOT NULL DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
                                  `msg` varchar(255) DEFAULT '' COMMENT '提示消息',
                                  `login_time` datetime NOT NULL COMMENT '访问时间',
                                  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                  `create_by` varchar(34)          DEFAULT NULL COMMENT '创建者用户ID',
                                  `update_by` varchar(34)          DEFAULT NULL COMMENT '更新者用户ID',
                                  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
                                  PRIMARY KEY (`id`),
                                  KEY `idx_sys_login_info_time` (`tenant_id`,`login_time`),
                                  KEY `idx_sys_login_info_status` (`tenant_id`,`status`),
                                  KEY `idx_sys_login_info_user` (`tenant_id`,`user_name`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统访问记录';


-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
                            `tenant_id` varchar(32) NOT NULL DEFAULT '0' COMMENT '租户编号',
                            `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
                            `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父菜单ID',
                            `order_num` int NOT NULL DEFAULT '0' COMMENT '显示顺序',
                            `path` varchar(200) NOT NULL DEFAULT '' COMMENT '路由地址',
                            `component` varchar(255) DEFAULT NULL COMMENT '组件路径',
                            `is_frame` int NOT NULL DEFAULT '1' COMMENT '是否为外链（0是 1否）',
                            `menu_type` char(1) NOT NULL DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
                            `visible` char(1) NOT NULL DEFAULT '0' COMMENT '显示状态（0显示 1隐藏）',
                            `status` char(1) NOT NULL DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
                            `is_default` tinyint NOT NULL DEFAULT 1 COMMENT '是否默认数据（0=系统默认不可删除，1=普通数据可删除）',
                            `perms` varchar(100) DEFAULT NULL COMMENT '权限标识',
                            `icon` varchar(100) NOT NULL DEFAULT '#' COMMENT '菜单图标',
                            `remark` varchar(500) DEFAULT '' COMMENT '备注',
                            `create_by` varchar(34) DEFAULT NULL COMMENT '创建者用户ID',
                            `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                            `update_by` varchar(34) DEFAULT NULL COMMENT '更新者用户ID',
                            `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                            `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
                            PRIMARY KEY (`id`),
                            KEY `idx_sys_menu_parent` (`parent_id`),
                            KEY `idx_sys_menu_status` (`status`),
                            KEY `idx_sys_menu_perms` (`perms`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单权限表';

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
BEGIN;
INSERT INTO `sys_menu` (`id`, `tenant_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`,
                        `menu_type`, `visible`, `status`, `perms`, `icon`, `remark`, `create_by`, `create_time`,
                        `update_by`, `update_time`, `deleted`)
VALUES (1, '0', '工作台', 0, 1, '/dashboard', 'dashboard/index', 1, 'C', '0', '0', 'dashboard:view', 'dashboard',
        '工作台菜单', NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `tenant_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`,
                        `menu_type`, `visible`, `status`, `perms`, `icon`, `remark`, `create_by`, `create_time`,
                        `update_by`, `update_time`, `deleted`)
VALUES (2, '0', '租户管理', 0, 2, '/tenant', 'tenant/index', 1, 'C', '0', '0', 'tenant:list', 'tenant', '租户管理菜单',
        NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `tenant_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`,
                        `menu_type`, `visible`, `status`, `perms`, `icon`, `remark`, `create_by`, `create_time`,
                        `update_by`, `update_time`, `deleted`)
VALUES (3, '0', '系统管理', 0, 3, '', '', 1, 'C', '0', '0', '', 'system', '系统管理菜单', NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `tenant_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`,
                        `menu_type`, `visible`, `status`, `perms`, `icon`, `remark`, `create_by`, `create_time`,
                        `update_by`, `update_time`, `deleted`)
VALUES (10, '0', '用户管理', 3, 1, '/system/user', 'system/user/index', 1, 'C', '0', '0', 'system:user:index', 'doctor',
        '用户管理菜单', NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `tenant_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`,
                        `menu_type`, `visible`, `status`, `perms`, `icon`, `remark`, `create_by`, `create_time`,
                        `update_by`, `update_time`, `deleted`)
VALUES (11, '0', '角色管理', 3, 2, '/system/role', 'system/role/index', 1, 'C', '0', '0', 'system:role:index', 'user',
        '角色管理菜单', NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `tenant_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`,
                        `menu_type`, `visible`, `status`, `perms`, `icon`, `remark`, `create_by`, `create_time`,
                        `update_by`, `update_time`, `deleted`)
VALUES (12, '0', '字典管理', 3, 3, '/system/dict', 'system/dict/index', 1, 'C', '0', '0', 'system:dict:index',
        'peoples', '字典管理菜单', NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `tenant_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`,
                        `menu_type`, `visible`, `status`, `perms`, `icon`, `remark`, `create_by`, `create_time`,
                        `update_by`, `update_time`, `deleted`)
VALUES (13, '0', '参数管理', 3, 4, '/system/config', 'system/config/index', 1, 'C', '0', '0', 'system:config:index',
        'tree-table', '参数管理菜单', NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `tenant_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`,
                        `menu_type`, `visible`, `status`, `perms`, `icon`, `remark`, `create_by`, `create_time`,
                        `update_by`, `update_time`, `deleted`)
VALUES (14, '0', '岗位管理', 3, 5, '/system/post', 'system/post/index', 1, 'C', '0', '0', 'system:post:index', 'dict',
        '岗位管理菜单', NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `tenant_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`,
                        `menu_type`, `visible`, `status`, `perms`, `icon`, `remark`, `create_by`, `create_time`,
                        `update_by`, `update_time`, `deleted`)
VALUES (15, '0', '部门配置', 3, 6, '/system/dept', 'system/dept/index', 1, 'C', '0', '0', 'system:dept:index', 'edit',
        '部门配置菜单', NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `tenant_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`,
                        `menu_type`, `visible`, `status`, `perms`, `icon`, `remark`, `create_by`, `create_time`,
                        `update_by`, `update_time`, `deleted`)
VALUES (16, '0', '套餐管理', 3, 7, '/system/tenant-package', 'system/tenant-package/index', 1, 'C', '0', '0',
        'system:tenant-package:index', 'post', '套餐菜单', NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `tenant_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`,
                        `menu_type`, `visible`, `status`, `perms`, `icon`, `remark`, `create_by`, `create_time`,
                        `update_by`, `update_time`, `deleted`)
VALUES (17, '0', '通知公告', 3, 8, '/system/notice', 'system/notice/index', 1, 'C', '0', '0', 'system:notice:index',
        'post', '通知公告', NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` (`id`, `tenant_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`,
                        `menu_type`, `visible`, `status`, `perms`, `icon`, `remark`, `create_by`, `create_time`,
                        `update_by`, `update_time`, `deleted`)
VALUES (18, '0', '系统日志', 3, 9, '/system/logs', 'system/logs/index', 1, 'C', '0', '0', 'system:logs:index', 'post',
        '日志管理菜单', NULL, NULL, NULL, NULL, 0);
UPDATE `sys_menu` SET `is_default` = 0 WHERE `tenant_id` = '0';
COMMIT;

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '公告ID',
                              `tenant_id` varchar(32) NOT NULL DEFAULT '0' COMMENT '租户编号',
                              `notice_title` varchar(50) NOT NULL COMMENT '公告标题',
                              `notice_type` char(1) NOT NULL COMMENT '公告类型（1通知 2公告）',
                              `notice_content` longtext COMMENT '公告内容',
                              `status` char(1) NOT NULL DEFAULT '0' COMMENT '公告状态（0启用 1停用）',
                              `remark` varchar(255) DEFAULT NULL COMMENT '备注',
                              `create_by` varchar(34) DEFAULT NULL COMMENT '创建者用户ID',
                              `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                              `update_by` varchar(34) DEFAULT NULL COMMENT '更新者用户ID',
                              `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                              `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
                              PRIMARY KEY (`id`),
                              KEY `idx_sys_notice_type_status` (`tenant_id`,`notice_type`,`status`),
                              KEY `idx_sys_notice_create_time` (`tenant_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知公告表';

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for sys_operator_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_operator_log`;
CREATE TABLE `sys_operator_log` (
                                    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
                                    `tenant_id` varchar(32) NOT NULL DEFAULT '0' COMMENT '租户编号',
                                    `title` varchar(50) NOT NULL DEFAULT '' COMMENT '模块标题',
                                    `business_type` int NOT NULL DEFAULT '0' COMMENT '业务类型（0其它 1新增 2修改 3删除）',
                                    `method` varchar(100) NOT NULL DEFAULT '' COMMENT '方法名称',
                                    `request_method` varchar(10) NOT NULL DEFAULT '' COMMENT '请求方式',
                                    `operator_type` int NOT NULL DEFAULT '0' COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
                                    `operator_name` varchar(50) DEFAULT '' COMMENT '操作人员',
                                    `dept_name` varchar(50) DEFAULT '' COMMENT '部门名称',
                                    `operator_url` varchar(255) DEFAULT '' COMMENT '请求URL',
                                    `operator_ip` varchar(128) DEFAULT '' COMMENT '主机地址',
                                    `operator_param` text COMMENT '请求参数',
                                    `json_result` text COMMENT '返回参数',
                                    `status` int NOT NULL DEFAULT '0' COMMENT '操作状态（0正常 1异常）',
                                    `error_msg` text COMMENT '错误消息',
                                    `operator_time` datetime NOT NULL COMMENT '操作时间',
                                    `cost_time` bigint NOT NULL DEFAULT '0' COMMENT '消耗时间',
                                    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                    `create_by` varchar(34)          DEFAULT NULL COMMENT '创建者用户ID',
                                    `update_by` varchar(34)          DEFAULT NULL COMMENT '更新者用户ID',
                                    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
                                    PRIMARY KEY (`id`),
                                    KEY `idx_sys_operator_log_business` (`tenant_id`,`business_type`),
                                    KEY `idx_sys_operator_log_time` (`tenant_id`,`operator_time`),
                                    KEY `idx_sys_operator_log_status` (`tenant_id`,`status`),
                                    KEY `idx_sys_operator_log_name` (`tenant_id`,`operator_name`)
) ENGINE=InnoDB AUTO_INCREMENT=790 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志记录';

-- ----------------------------

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
                            `tenant_id` varchar(32) NOT NULL DEFAULT '0' COMMENT '租户编号',
                            `post_code` varchar(64) NOT NULL COMMENT '岗位编码',
                            `post_name` varchar(50) NOT NULL COMMENT '岗位名称',
                            `order_num` int NOT NULL COMMENT '显示顺序',
                            `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                            `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
                            `create_by` varchar(34) DEFAULT NULL COMMENT '创建者用户ID',
                            `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                            `update_by` varchar(34) DEFAULT NULL COMMENT '更新者用户ID',
                            `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                            `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_sys_post_code` (`tenant_id`,`post_code`),
                            KEY `idx_sys_post_status` (`tenant_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='岗位信息表';

-- ----------------------------
-- Records of sys_post
-- ----------------------------
BEGIN;
INSERT INTO `sys_post` (`id`, `tenant_id`, `post_code`, `post_name`, `order_num`, `remark`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`) VALUES (1, '0', 'OPERATIONS_ADMIN', '运营管理员', 1, '负责平台日常运营', 0, NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_post` (`id`, `tenant_id`, `post_code`, `post_name`, `order_num`, `remark`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`) VALUES (2, '0', 'PHARMACY_MANAGER', '药房主管', 2, '负责药品库存和发药', 0, NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_post` (`id`, `tenant_id`, `post_code`, `post_name`, `order_num`, `remark`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`) VALUES (3, '0', 'SERVICE_AGENT', '客服专员', 3, '负责患者咨询和预约协助', 0, NULL, NULL, NULL, NULL, 0);
COMMIT;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
                            `tenant_id` varchar(32) NOT NULL DEFAULT '0' COMMENT '租户编号',
                            `role_name` varchar(30) NOT NULL COMMENT '角色名称',
                            `role_code` varchar(100) NOT NULL COMMENT '角色权限字符串',
                            `order_num` int NOT NULL COMMENT '显示顺序',
                            `data_scope` tinyint NOT NULL DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限 5：仅本人数据权限 6：部门及以下或本人数据权限）',
                            `status` tinyint NOT NULL DEFAULT '0' COMMENT '角色状态（0正常 1停用）',
                            `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                            `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
                            `create_by` varchar(34) DEFAULT NULL COMMENT '创建者用户ID',
                            `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                            `update_by` varchar(34) DEFAULT NULL COMMENT '更新者用户ID',
                            `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_sys_role_code` (`tenant_id`,`role_code`),
                            KEY `idx_sys_role_status` (`tenant_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色信息表';

-- ----------------------------
-- Records of sys_role
-- ----------------------------
BEGIN;
INSERT INTO `sys_role` (`id`, `tenant_id`, `role_name`, `role_code`, `order_num`, `data_scope`, `status`, `remark`, `deleted`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (1, '0', '系统管理员', 'SYSTEM_ADMIN', 1, 1, 0, '系统初始化管理员角色', 0, NULL, NULL, NULL, '2026-06-19 19:40:34');
INSERT INTO `sys_role` (`id`, `tenant_id`, `role_name`, `role_code`, `order_num`, `data_scope`, `status`, `remark`,
                        `deleted`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, '0', '运营管理员', 'OPERATOR_ADMIN', 2, 2, 0, '系统初始化运营角色', 1, NULL, NULL, NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `tenant_id` varchar(32) NOT NULL DEFAULT '0' COMMENT '租户编号',
                                 `role_id` bigint NOT NULL COMMENT '角色ID',
                                 `dept_id` bigint NOT NULL COMMENT '部门ID',
                                 `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                 `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                 `create_by` varchar(34)          DEFAULT NULL COMMENT '创建者用户ID',
                                 `update_by` varchar(34)          DEFAULT NULL COMMENT '更新者用户ID',
                                 `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_sys_role_dept` (`tenant_id`,`role_id`,`dept_id`),
                                 KEY `idx_sys_role_dept_dept` (`tenant_id`,`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色和部门关联表';

-- ----------------------------
-- Records of sys_role_dept
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `tenant_id` varchar(32) NOT NULL DEFAULT '0' COMMENT '租户编号',
                                 `role_id` bigint NOT NULL COMMENT '角色ID',
                                 `menu_id` bigint NOT NULL COMMENT '菜单ID',
                                 `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                 `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                 `create_by` varchar(34)          DEFAULT NULL COMMENT '创建者用户ID',
                                 `update_by` varchar(34)          DEFAULT NULL COMMENT '更新者用户ID',
                                 `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_sys_role_menu` (`tenant_id`,`role_id`,`menu_id`),
                                 KEY `idx_sys_role_menu_menu` (`tenant_id`,`menu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色和菜单关联表';

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
BEGIN;
INSERT INTO `sys_role_menu` (`id`, `tenant_id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`,
                             `update_by`, `deleted`)
VALUES (1, '0', 1, 10, NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` (`id`, `tenant_id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`,
                             `update_by`, `deleted`)
VALUES (2, '0', 1, 11, NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` (`id`, `tenant_id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`,
                             `update_by`, `deleted`)
VALUES (3, '0', 1, 12, NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` (`id`, `tenant_id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`,
                             `update_by`, `deleted`)
VALUES (4, '0', 1, 13, NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` (`id`, `tenant_id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`,
                             `update_by`, `deleted`)
VALUES (5, '0', 1, 14, NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` (`id`, `tenant_id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`,
                             `update_by`, `deleted`)
VALUES (6, '0', 1, 15, NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` (`id`, `tenant_id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`,
                             `update_by`, `deleted`)
VALUES (7, '0', 1, 16, NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` (`id`, `tenant_id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`,
                             `update_by`, `deleted`)
VALUES (8, '0', 1, 17, NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_role_menu` (`id`, `tenant_id`, `role_id`, `menu_id`, `create_time`, `update_time`, `create_by`,
                             `update_by`, `deleted`)
VALUES (13, '0', 1, 18, NULL, NULL, NULL, NULL, 0);
COMMIT;

-- ----------------------------
-- Table structure for sys_tenant
-- ----------------------------
DROP TABLE IF EXISTS `sys_tenant`;
CREATE TABLE `sys_tenant` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                              `tenant_id` varchar(32) NOT NULL COMMENT '租户编号',
                              `contact_user_name` varchar(20) DEFAULT NULL COMMENT '联系人',
                              `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
                              `company_name` varchar(30) NOT NULL COMMENT '企业名称',
                              `license_number` varchar(30) DEFAULT NULL COMMENT '统一社会信用代码',
                              `address` varchar(200) DEFAULT NULL COMMENT '地址',
                              `intro` varchar(200) DEFAULT NULL COMMENT '企业简介',
                              `domain` varchar(200) DEFAULT NULL COMMENT '域名',
                              `remark` varchar(200) DEFAULT NULL COMMENT '备注',
                              `package_id` bigint DEFAULT NULL COMMENT '租户套餐编号',
                              `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
                              `account_count` int NOT NULL DEFAULT '-1' COMMENT '用户数量（-1不限制）',
                              `status` char(1) NOT NULL DEFAULT '0' COMMENT '租户状态（0启用 1禁用）',
                              `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
                              `create_by` varchar(34) DEFAULT NULL COMMENT '创建者用户ID',
                              `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                              `update_by` varchar(34) DEFAULT NULL COMMENT '更新者用户ID',
                              `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_sys_tenant_id` (`tenant_id`),
                              UNIQUE KEY `uk_sys_tenant_license` (`license_number`),
                              UNIQUE KEY `uk_sys_tenant_domain` (`domain`),
                              KEY `idx_sys_tenant_package` (`package_id`),
                              KEY `idx_sys_tenant_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租户表';

-- ----------------------------
-- Records of sys_tenant
-- ----------------------------
BEGIN;
INSERT INTO `sys_tenant` (`id`, `tenant_id`, `contact_user_name`, `contact_phone`, `company_name`, `license_number`, `address`, `intro`, `domain`, `remark`, `package_id`, `expire_time`, `account_count`, `status`, `deleted`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (1, '0', 'hlw_admin', '13800001111', '互联网医院平台', '91330000HLW000001X', '杭州市西湖区互联网医院园区', '互联网医院平台默认租户', 'hlw.local', NULL, 1, '2026-12-31 23:59:59', -1, '0', 0, NULL, NULL, NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for sys_tenant_package
-- ----------------------------
DROP TABLE IF EXISTS `sys_tenant_package`;
CREATE TABLE `sys_tenant_package` (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT '租户套餐ID',
                                      `tenant_id` varchar(32) NOT NULL DEFAULT '0' COMMENT '租户编号',
                                      `package_name` varchar(20) NOT NULL COMMENT '套餐名称',
                                      `remark` varchar(200) DEFAULT NULL COMMENT '备注',
                                      `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
                                      `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
                                      `create_by` varchar(34) DEFAULT NULL COMMENT '创建者用户ID',
                                      `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                      `update_by` varchar(34) DEFAULT NULL COMMENT '更新者用户ID',
                                      `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_sys_tenant_package_name` (`package_name`),
                                      KEY `idx_sys_tenant_package_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租户套餐表';

-- ----------------------------
-- Records of sys_tenant_package
-- ----------------------------
BEGIN;
INSERT INTO `sys_tenant_package` (`id`, `tenant_id`, `package_name`, `remark`, `status`, `deleted`, `create_by`,
                                  `create_time`, `update_by`, `update_time`)
VALUES (1, '0', '默认套餐', '默认包含基础系统菜单', 0, 0, NULL, NULL, NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for sys_tenant_package_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_tenant_package_menu`;
CREATE TABLE `sys_tenant_package_menu` (
                                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                           `tenant_id` varchar(32) NOT NULL DEFAULT '0' COMMENT '租户编号',
                                           `package_id` bigint NOT NULL COMMENT '租户套餐ID',
                                           `menu_id` bigint NOT NULL COMMENT '菜单ID',
                                           `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                           `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                           `create_by` varchar(34) DEFAULT NULL COMMENT '创建者用户ID',
                                           `update_by` varchar(34) DEFAULT NULL COMMENT '更新者用户ID',
                                           `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
                                           PRIMARY KEY (`id`),
                                           UNIQUE KEY `uk_sys_tenant_package_menu` (`tenant_id`,`package_id`,`menu_id`),
                                           KEY `idx_sys_tenant_package_menu_menu` (`menu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租户套餐和菜单关联表';

-- ----------------------------
-- Records of sys_tenant_package_menu
-- ----------------------------
BEGIN;
INSERT INTO `sys_tenant_package_menu` (`id`, `tenant_id`, `package_id`, `menu_id`, `create_time`, `update_time`,
                                       `create_by`, `update_by`, `deleted`)
VALUES (1, '0', 1, 1, NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_tenant_package_menu` (`id`, `tenant_id`, `package_id`, `menu_id`, `create_time`, `update_time`,
                                       `create_by`, `update_by`, `deleted`)
VALUES (2, '0', 1, 2, NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_tenant_package_menu` (`id`, `tenant_id`, `package_id`, `menu_id`, `create_time`, `update_time`,
                                       `create_by`, `update_by`, `deleted`)
VALUES (3, '0', 1, 3, NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_tenant_package_menu` (`id`, `tenant_id`, `package_id`, `menu_id`, `create_time`, `update_time`,
                                       `create_by`, `update_by`, `deleted`)
VALUES (4, '0', 1, 4, NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_tenant_package_menu` (`id`, `tenant_id`, `package_id`, `menu_id`, `create_time`, `update_time`,
                                       `create_by`, `update_by`, `deleted`)
VALUES (5, '0', 1, 5, NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_tenant_package_menu` (`id`, `tenant_id`, `package_id`, `menu_id`, `create_time`, `update_time`,
                                       `create_by`, `update_by`, `deleted`)
VALUES (6, '0', 1, 6, NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_tenant_package_menu` (`id`, `tenant_id`, `package_id`, `menu_id`, `create_time`, `update_time`,
                                       `create_by`, `update_by`, `deleted`)
VALUES (7, '0', 1, 7, NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_tenant_package_menu` (`id`, `tenant_id`, `package_id`, `menu_id`, `create_time`, `update_time`,
                                       `create_by`, `update_by`, `deleted`)
VALUES (8, '0', 1, 8, NULL, NULL, NULL, NULL, 0);
COMMIT;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户表ID',
                            `user_id` varchar(34) NOT NULL COMMENT '用户ID（格式 U_ + 32 位 UUID）',
                            `tenant_id` varchar(32) NOT NULL DEFAULT '0' COMMENT '租户编号',
                            `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
                            `user_name` varchar(30) NOT NULL COMMENT '用户账号',
                            `real_name` varchar(30) NOT NULL DEFAULT '' COMMENT '真实姓名',
                            `nick_name` varchar(30) NOT NULL COMMENT '用户昵称',
                            `user_type` varchar(10) NOT NULL DEFAULT 'sys_user' COMMENT '用户类型',
                            `email` varchar(50) DEFAULT '' COMMENT '用户邮箱',
                            `phone` varchar(11) DEFAULT '' COMMENT '手机号码',
                            `sex` char(1) NOT NULL DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
                            `avatar` bigint DEFAULT NULL COMMENT '头像地址',
                            `password` varchar(100) NOT NULL DEFAULT '' COMMENT '密码',
                            `status` tinyint NOT NULL DEFAULT '0' COMMENT '账号状态（0正常 1停用）',
                            `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
                            `login_ip` varchar(128) DEFAULT '' COMMENT '最后登录IP',
                            `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
                            `create_by` varchar(34) DEFAULT NULL COMMENT '创建者用户ID',
                            `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                            `update_by` varchar(34) DEFAULT NULL COMMENT '更新者用户ID',
                            `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                            `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_sys_user_id` (`tenant_id`,`user_id`),
                            UNIQUE KEY `uk_sys_user_name` (`tenant_id`,`user_name`),
                            KEY `idx_sys_user_dept` (`tenant_id`,`dept_id`),
                            KEY `idx_sys_user_phone` (`tenant_id`,`phone`),
                            KEY `idx_sys_user_status` (`tenant_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户信息表';

-- ----------------------------
-- Records of sys_user
-- ----------------------------
BEGIN;
INSERT INTO `sys_user` (`id`, `user_id`, `tenant_id`, `dept_id`, `user_name`, `real_name`, `nick_name`, `user_type`, `email`, `phone`, `sex`, `avatar`, `password`, `status`, `deleted`, `login_ip`, `login_date`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, 'U_550e8400e29b41d4a716446655440001', '0', 1, 'hlw_admin', '平台超级管理员', '平台超级管理员', 'sys_user', 'ops@hlw.local', '13800001111', '2', NULL, '$2a$10$ixRO//u86BmCszxCmA8q/uZcomXfS1qaTs0e1drI4bwl1/CPX.kU2', 0, 0, '127.0.0.1', '2026-06-19 09:21:52', NULL, NULL, NULL, NULL, '默认平台超管账号');
INSERT INTO `sys_user` (`id`, `user_id`, `tenant_id`, `dept_id`, `user_name`, `real_name`, `nick_name`, `user_type`, `email`, `phone`, `sex`, `avatar`, `password`, `status`, `deleted`, `login_ip`, `login_date`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, 'U_550e8400e29b41d4a716446655440102', '100', NULL, 'patient_zhao', '赵晓岚', '赵晓岚', 'patient', NULL, '13900001111', '1', NULL, '$2a$10$ixRO//u86BmCszxCmA8q/uZcomXfS1qaTs0e1drI4bwl1/CPX.kU2', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '默认患者演示账号');
INSERT INTO `sys_user` (`id`, `user_id`, `tenant_id`, `dept_id`, `user_name`, `real_name`, `nick_name`, `user_type`, `email`, `phone`, `sex`, `avatar`, `password`, `status`, `deleted`, `login_ip`, `login_date`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3, 'U_550e8400e29b41d4a716446655440103', '100', NULL, 'patient_shen', '沈博远', '沈博远', 'patient', NULL, '13900002222', '0', NULL, '$2a$10$ixRO//u86BmCszxCmA8q/uZcomXfS1qaTs0e1drI4bwl1/CPX.kU2', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '默认患者演示账号');
INSERT INTO `sys_user` (`id`, `user_id`, `tenant_id`, `dept_id`, `user_name`, `real_name`, `nick_name`, `user_type`, `email`, `phone`, `sex`, `avatar`, `password`, `status`, `deleted`, `login_ip`, `login_date`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4, 'U_550e8400e29b41d4a716446655440104', '100', NULL, 'doctor_chen', '陈知衡', '陈知衡', 'doctor', NULL, '13900003333', '0', NULL, '$2a$10$ixRO//u86BmCszxCmA8q/uZcomXfS1qaTs0e1drI4bwl1/CPX.kU2', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '默认医生演示账号');
INSERT INTO `sys_user` (`id`, `user_id`, `tenant_id`, `dept_id`, `user_name`, `real_name`, `nick_name`, `user_type`, `email`, `phone`, `sex`, `avatar`, `password`, `status`, `deleted`, `login_ip`, `login_date`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5, 'U_550e8400e29b41d4a716446655440105', '100', NULL, 'doctor_gu', '顾清和', '顾清和', 'doctor', NULL, '13900004444', '0', NULL, '$2a$10$ixRO//u86BmCszxCmA8q/uZcomXfS1qaTs0e1drI4bwl1/CPX.kU2', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '默认医生演示账号');
COMMIT;

-- ----------------------------
-- Table structure for sys_user_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `tenant_id` varchar(32) NOT NULL DEFAULT '0' COMMENT '租户编号',
                                 `user_id` varchar(34) NOT NULL COMMENT '用户ID（格式 U_ + 32 位 UUID）',
                                 `post_id` bigint NOT NULL COMMENT '岗位ID',
                                 `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                 `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                 `create_by` varchar(34)          DEFAULT NULL COMMENT '创建者用户ID',
                                 `update_by` varchar(34)          DEFAULT NULL COMMENT '更新者用户ID',
                                 `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_sys_user_post` (`tenant_id`,`user_id`,`post_id`),
                                 KEY `idx_sys_user_post_post` (`tenant_id`,`post_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户与岗位关联表';

-- ----------------------------
-- Records of sys_user_post
-- ----------------------------
BEGIN;
INSERT INTO `sys_user_post` (`id`, `tenant_id`, `user_id`, `post_id`, `create_time`, `update_time`, `create_by`,
                             `update_by`, `deleted`)
VALUES (1, '0', 'U_550e8400e29b41d4a716446655440001', 1, NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_user_post` (`id`, `tenant_id`, `user_id`, `post_id`, `create_time`, `update_time`, `create_by`,
                             `update_by`, `deleted`)
VALUES (2, '0', 'U_550e8400e29b41d4a716446655440002', 2, NULL, NULL, NULL, NULL, 0);
COMMIT;

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `tenant_id` varchar(32) NOT NULL DEFAULT '0' COMMENT '租户编号',
                                 `user_id` varchar(34) NOT NULL COMMENT '用户ID（格式 U_ + 32 位 UUID）',
                                 `role_id` bigint NOT NULL COMMENT '角色ID',
                                 `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                 `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                 `create_by` varchar(34)          DEFAULT NULL COMMENT '创建者用户ID',
                                 `update_by` varchar(34)          DEFAULT NULL COMMENT '更新者用户ID',
                                 `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_sys_user_role` (`tenant_id`,`user_id`,`role_id`),
                                 KEY `idx_sys_user_role_role` (`tenant_id`,`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户和角色关联表';

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
BEGIN;
INSERT INTO `sys_user_role` (`id`, `tenant_id`, `user_id`, `role_id`, `create_time`, `update_time`, `create_by`,
                             `update_by`, `deleted`)
VALUES (1, '0', 'U_550e8400e29b41d4a716446655440001', 1, NULL, NULL, NULL, NULL, 0);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;



USE hospital_patient;

CREATE TABLE IF NOT EXISTS pat_patient (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号' PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '租户编号',
    user_id BIGINT NOT NULL COMMENT '关联用户编号（关联sys_user.id）',
    name VARCHAR(64) NOT NULL COMMENT '兼容旧表患者姓名',
    patient_name VARCHAR(64) NOT NULL DEFAULT '' COMMENT '患者姓名',
    phone VARCHAR(32) COMMENT '联系电话',
    gender VARCHAR(16) NOT NULL DEFAULT '' COMMENT '患者性别',
    age INT NOT NULL DEFAULT 0 COMMENT '患者年龄',
    risk_level VARCHAR(32) NOT NULL DEFAULT '低风险' COMMENT '风险等级',
    id_card VARCHAR(32) COMMENT '身份证号',
    birthday DATE COMMENT '出生日期',
    address VARCHAR(512) COMMENT '联系地址',
    last_visit DATE COMMENT '最近就诊日期',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人编号',
    update_by BIGINT COMMENT '更新人编号',
    deleted SMALLINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
    KEY idx_pat_patient_user (`tenant_id`, `user_id`)
) COMMENT='患者档案表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS pat_health_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号' PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '租户编号',
    patient_id BIGINT NOT NULL COMMENT '患者档案编号（关联pat_patient.id）',
    title VARCHAR(128) NOT NULL DEFAULT '' COMMENT '档案标题',
    summary VARCHAR(256) NOT NULL DEFAULT '' COMMENT '档案摘要',
    allergies TEXT COMMENT '过敏史',
    history TEXT COMMENT '既往病史',
    diagnosis TEXT COMMENT '诊断信息',
    remark VARCHAR(512) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人编号',
    update_by BIGINT COMMENT '更新人编号',
    deleted SMALLINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识'
) COMMENT='健康档案表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

UPDATE pat_health_record
SET title = COALESCE(NULLIF(title, ''), '历史健康档案'),
    summary = COALESCE(NULLIF(summary, ''), COALESCE(NULLIF(diagnosis, ''), NULLIF(history, ''), NULLIF(remark, ''), '历史健康档案'))
WHERE title = '' OR summary = '';
UPDATE pat_patient
SET patient_name = COALESCE(NULLIF(patient_name, ''), name),
    age = COALESCE(age, 0),
    risk_level = COALESCE(NULLIF(risk_level, ''), '低风险');

INSERT INTO pat_patient (id, tenant_id, user_id, name, patient_name, phone, gender, age, risk_level, id_card, birthday, address, last_visit)
VALUES
    (1, 100, 2, '赵晓岚', '赵晓岚', '13900001111', '女', 34, '中风险', '110101199201010011', '1992-01-01', '杭州市西湖区', '2026-06-11'),
    (2, 100, 3, '沈博远', '沈博远', '13900002222', '男', 58, '高风险', '110101196801010022', '1968-01-01', '杭州市滨江区', '2026-06-10')
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO pat_health_record (id, tenant_id, patient_id, title, summary, history, diagnosis, remark)
VALUES
    (1, 100, 1, '发热问诊', '儿童发热 12 小时，已线上问诊', '无特殊既往史', '上呼吸道感染待观察', '演示健康档案'),
    (2, 100, 2, '复诊续方', '慢病用药复诊记录', '高血压慢病管理', '血压控制稳定', '演示健康档案')
ON DUPLICATE KEY UPDATE id = id;

CREATE TABLE IF NOT EXISTS local_message (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号' PRIMARY KEY,
    topic VARCHAR(128) NOT NULL COMMENT '消息主题',
    body TEXT NOT NULL COMMENT '消息内容',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    max_retry INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    next_retry_time DATETIME COMMENT '下次重试时间',
    -- status: PENDING, SENT, FAILED
    status VARCHAR(16) NOT NULL COMMENT '消息状态',
    error_msg TEXT COMMENT '错误信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='本地消息表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

USE hospital_doctor;

CREATE TABLE IF NOT EXISTS doc_department (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号' PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '租户编号',
    name VARCHAR(128) NOT NULL COMMENT '兼容旧表科室名称',
    department_name VARCHAR(64) NOT NULL DEFAULT '' COMMENT '科室名称',
    doctor_count INT NOT NULL DEFAULT 0 COMMENT '医生数量',
    queue_desc VARCHAR(64) NOT NULL DEFAULT '' COMMENT '排队描述',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父级科室编号',
    sort INT NOT NULL DEFAULT 0 COMMENT '科室排序',
    status VARCHAR(32) NOT NULL DEFAULT '0' COMMENT '科室状态',
    description VARCHAR(512) COMMENT '科室说明',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人编号',
    update_by BIGINT COMMENT '更新人编号',
    deleted SMALLINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识'
) COMMENT='科室信息表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

UPDATE doc_department SET department_name = name WHERE department_name = '' AND name <> '';

CREATE TABLE IF NOT EXISTS doc_doctor (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号' PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '关联用户编号（关联sys_user.id）',
    tenant_id BIGINT NOT NULL COMMENT '租户编号',
    name VARCHAR(64) NOT NULL COMMENT '医生姓名',
    avatar VARCHAR(512) COMMENT '头像地址',
    title VARCHAR(64) COMMENT '医生职称',
    specialty VARCHAR(512) COMMENT '擅长方向',
    introduction TEXT COMMENT '医生简介',
    consult_fee DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '问诊费用',
    -- consult_status: 0 offline, 1 online, 2 busy
    consult_status SMALLINT NOT NULL DEFAULT 0 COMMENT '接诊状态',
    rating DECIMAL(3, 2) NOT NULL DEFAULT 0 COMMENT '评分',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人编号',
    update_by BIGINT COMMENT '更新人编号',
    deleted SMALLINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
    KEY idx_doc_doctor_user (`tenant_id`, `user_id`)
) COMMENT='医生信息表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS doc_doctor_department (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号' PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '租户编号',
    doctor_id BIGINT NOT NULL COMMENT '医生编号',
    department_id BIGINT NOT NULL COMMENT '科室编号',
    -- is_free: 0 paid, 1 free
    is_free SMALLINT NOT NULL DEFAULT 0 COMMENT '是否免挂号费',
    appointment_fee DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '挂号费用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人编号',
    update_by BIGINT COMMENT '更新人编号',
    deleted SMALLINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识'
) COMMENT='医生科室关联表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS doc_schedule (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号' PRIMARY KEY,
    doctor_id BIGINT NOT NULL COMMENT '医生编号',
    tenant_id BIGINT NOT NULL COMMENT '租户编号',
    schedule_date DATE NOT NULL COMMENT '排班日期',
    time_slot VARCHAR(64) NOT NULL COMMENT '排班时间段',
    total_number INT NOT NULL DEFAULT 0 COMMENT '总号源数量',
    remain_number INT NOT NULL DEFAULT 0 COMMENT '剩余号源数量',
    -- status: 0 disabled, 1 available, 2 full
    status SMALLINT NOT NULL DEFAULT 1 COMMENT '排班状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人编号',
    update_by BIGINT COMMENT '更新人编号',
    deleted SMALLINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识'
) COMMENT='医生排班表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS local_message (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号' PRIMARY KEY,
    topic VARCHAR(128) NOT NULL COMMENT '消息主题',
    body TEXT NOT NULL COMMENT '消息内容',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    max_retry INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    next_retry_time DATETIME COMMENT '下次重试时间',
    -- status: PENDING, SENT, FAILED
    status VARCHAR(16) NOT NULL COMMENT '消息状态',
    error_msg TEXT COMMENT '错误信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='本地消息表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DELETE current_row
FROM doc_doctor_department current_row
JOIN doc_doctor_department kept_row
  ON current_row.doctor_id = kept_row.doctor_id
 AND current_row.department_id = kept_row.department_id
 AND kept_row.deleted = 0
WHERE current_row.id > kept_row.id
  AND current_row.deleted = 0;
CREATE UNIQUE INDEX uk_doc_doctor_department_active
ON doc_doctor_department (doctor_id, department_id, deleted);

INSERT INTO doc_department (id, tenant_id, name, department_name, doctor_count, queue_desc, parent_id, sort, status)
VALUES
    (10, 100, '心内科', '心内科', 1, '当前等候 6 人', 0, 1, '0'),
    (20, 100, '儿科', '儿科', 1, '当前等候 8 人', 0, 2, '0'),
    (30, 100, '皮肤科', '皮肤科', 0, '当前等候 3 人', 0, 3, '0')
ON DUPLICATE KEY UPDATE name = VALUES(name),
                               department_name = VALUES(department_name),
                               doctor_count = VALUES(doctor_count),
                               queue_desc = VALUES(queue_desc),
                               parent_id = VALUES(parent_id),
                               sort = VALUES(sort),
                               status = VALUES(status);

INSERT INTO doc_doctor (id, tenant_id, user_id, name, title, specialty, consult_fee, consult_status)
VALUES
    (1, 100, 4, '陈知衡', '主任医师', '冠脉慢病管理', 50.00, 1),
    (2, 100, 5, '顾清和', '副主任医师', '糖尿病营养干预', 30.00, 2)
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id),
                               name = VALUES(name),
                               title = VALUES(title),
                               specialty = VALUES(specialty),
                               consult_fee = VALUES(consult_fee),
                               consult_status = VALUES(consult_status);

INSERT INTO doc_doctor_department (id, tenant_id, doctor_id, department_id, is_free, appointment_fee)
VALUES
    (1, 100, 1, 10, 0, 50.00),
    (2, 100, 2, 20, 0, 30.00)
ON DUPLICATE KEY UPDATE is_free = VALUES(is_free),
              appointment_fee = VALUES(appointment_fee),
              update_time = CURRENT_TIMESTAMP;

USE hospital_consult;

CREATE TABLE IF NOT EXISTS con_consult (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号' PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '租户编号',
    patient_id BIGINT NOT NULL COMMENT '患者档案编号（关联pat_patient.id）',
    doctor_id BIGINT NOT NULL COMMENT '医生编号',
    -- consult_type: IMAGE_TEXT, QUICK, FOLLOW_UP
    consult_type VARCHAR(32) NOT NULL COMMENT '问诊类型',
    -- status: PENDING_PAY, WAITING, IN_PROGRESS, FINISHED, CANCELLED, TIMEOUT
    status SMALLINT NOT NULL DEFAULT 0 COMMENT '问诊状态',
    fee_amount DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '问诊费用',
    duration_limit INT NOT NULL DEFAULT 0 COMMENT '问诊时长上限分钟',
    remaining_seconds INT NOT NULL DEFAULT 0 COMMENT '剩余问诊秒数',
    start_time DATETIME COMMENT '接单开始时间',
    end_time DATETIME COMMENT '问诊结束时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人编号',
    update_by BIGINT COMMENT '更新人编号',
    deleted SMALLINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识'
) COMMENT='问诊单表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS con_message (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号' PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '租户编号',
    consult_id BIGINT NOT NULL COMMENT '问诊编号',
    sender_id BIGINT NOT NULL COMMENT '发送人编号',
    -- sender_type: PATIENT, DOCTOR, SYSTEM
    sender_type VARCHAR(32) NOT NULL COMMENT '发送人类型',
    content TEXT COMMENT '消息内容',
    -- content_type: TEXT, IMAGE, SYSTEM
    content_type VARCHAR(32) NOT NULL DEFAULT 'TEXT' COMMENT '消息内容类型',
    -- is_read: 0 unread, 1 read
    is_read SMALLINT NOT NULL DEFAULT 0 COMMENT '兼容旧表已读标识',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人编号',
    update_by BIGINT COMMENT '更新人编号',
    deleted SMALLINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识'
) COMMENT='问诊消息表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS con_consult_image (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号' PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '租户编号',
    consult_id BIGINT NOT NULL COMMENT '问诊编号',
    message_id BIGINT COMMENT '消息编号',
    image_url VARCHAR(512) NOT NULL COMMENT '图片地址',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人编号',
    update_by BIGINT COMMENT '更新人编号',
    deleted SMALLINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识'
) COMMENT='问诊图片表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

UPDATE con_message SET read_flag = 1 WHERE read_flag = 0 AND is_read <> 0;

INSERT INTO con_consult (id, tenant_id, patient_id, doctor_id, consult_type, consult_no, patient_name, doctor_name, channel, status, updated_at)
VALUES
    (1, 100, 1, 1, 'IMAGE_TEXT', 'ZX20260612001', '赵晓岚', '陈知衡', '图文', '待接单', '10:18'),
    (2, 100, 2, 2, 'VIDEO', 'ZX20260612002', '沈博远', '顾清和', '视频', '咨询中', '10:07')
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO con_message (id, tenant_id, consult_id, sender_id, sender_type, content, content_type, read_flag, create_time)
VALUES
    (1, 100, 1, 2, 'DOCTOR', '哪里不舒服', 'TEXT', 0, '2026-06-13 10:15:00'),
    (2, 100, 1, 1, 'PATIENT', '孩子从昨晚开始发烧', 'TEXT', 0, '2026-06-13 10:16:00')
ON DUPLICATE KEY UPDATE id = id;

CREATE TABLE IF NOT EXISTS local_message (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号' PRIMARY KEY,
    topic VARCHAR(128) NOT NULL COMMENT '消息主题',
    body TEXT NOT NULL COMMENT '消息内容',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    max_retry INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    next_retry_time DATETIME COMMENT '下次重试时间',
    -- status: PENDING, SENT, FAILED
    status VARCHAR(16) NOT NULL COMMENT '消息状态',
    error_msg TEXT COMMENT '错误信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='本地消息表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

USE hospital_appointment;

CREATE TABLE IF NOT EXISTS apt_appointment (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号' PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '租户编号',
    patient_id BIGINT NOT NULL COMMENT '患者档案编号（关联pat_patient.id）',
    doctor_id BIGINT NOT NULL COMMENT '医生编号',
    department_id BIGINT NOT NULL COMMENT '科室编号',
    schedule_id BIGINT NOT NULL COMMENT '排班编号',
    number_source_id BIGINT COMMENT '号源编号',
    -- appointment_type: NORMAL, CONVENIENT
    appointment_type VARCHAR(32) NOT NULL COMMENT '预约类型',
    -- status: 0 pending pay, 1 booked, 2 checked in, 3 completed, 4 cancelled
    status SMALLINT NOT NULL DEFAULT 0 COMMENT '预约状态',
    fee_amount DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '预约费用',
    pay_time DATETIME COMMENT '支付时间',
    check_in_time DATETIME COMMENT '签到时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人编号',
    update_by BIGINT COMMENT '更新人编号',
    deleted SMALLINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识'
) COMMENT='预约单表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS apt_number_source (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号' PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '租户编号',
    schedule_id BIGINT NOT NULL COMMENT '排班编号',
    number_seq INT NOT NULL COMMENT '号源序号',
    -- status: 0 available, 1 locked, 2 used, 3 released
    status SMALLINT NOT NULL DEFAULT 0 COMMENT '号源状态',
    lock_time DATETIME COMMENT '锁定时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人编号',
    update_by BIGINT COMMENT '更新人编号',
    deleted SMALLINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识'
) COMMENT='预约号源表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS apt_number_source_release_config (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号' PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '租户编号',
    schedule_id BIGINT NOT NULL COMMENT '排班编号',
    release_time DATETIME NOT NULL COMMENT '放号时间',
    release_count INT NOT NULL DEFAULT 0 COMMENT '放号数量',
    -- status: 0 disabled, 1 enabled, 2 finished
    status SMALLINT NOT NULL DEFAULT 1 COMMENT '配置状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人编号',
    update_by BIGINT COMMENT '更新人编号',
    deleted SMALLINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识'
) COMMENT='放号配置表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO apt_appointment (id, tenant_id, patient_id, doctor_id, department_id, schedule_id, appointment_type, appointment_no, patient_name, doctor_name, clinic_time, source, status, fee_amount)
VALUES
    (1, 100, 1, 1, 10, 1, '普通门诊', 'YY20260612001', '赵晓岚', '陈知衡', '2026-06-13 14:00', '小程序', '待支付', 30.00),
    (2, 100, 2, 2, 20, 2, '普通门诊', 'YY20260612002', '沈博远', '顾清和', '2026-06-13 15:30', '客服代约', '已签到', 30.00)
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO apt_number_source (id, tenant_id, schedule_id, number_seq, status)
VALUES
    (1, 100, 1, 1, 'AVAILABLE'),
    (2, 100, 1, 2, 'AVAILABLE')
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO apt_number_source_release_config (id, tenant_id, schedule_id, release_time, release_count, status)
VALUES
    (1, 100, 1, '2026-06-13 08:00:00', 10, '0')
ON DUPLICATE KEY UPDATE id = id;

CREATE TABLE IF NOT EXISTS local_message (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号' PRIMARY KEY,
    topic VARCHAR(128) NOT NULL COMMENT '消息主题',
    body TEXT NOT NULL COMMENT '消息内容',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    max_retry INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    next_retry_time DATETIME COMMENT '下次重试时间',
    -- status: PENDING, SENT, FAILED
    status VARCHAR(16) NOT NULL COMMENT '消息状态',
    error_msg TEXT COMMENT '错误信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='本地消息表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

USE hospital_prescription;

CREATE TABLE IF NOT EXISTS pre_prescription (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号' PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '租户编号',
    consult_id BIGINT NOT NULL COMMENT '问诊编号',
    patient_id BIGINT NOT NULL COMMENT '患者档案编号（关联pat_patient.id）',
    doctor_id BIGINT NOT NULL COMMENT '医生编号',
    pharmacist_id BIGINT COMMENT '审核药师编号',
    -- status: 0 draft, 1 submitted, 2 approved, 3 rejected, 4 dispensed
    status SMALLINT NOT NULL DEFAULT 0 COMMENT '处方状态',
    audit_remark VARCHAR(512) COMMENT '审核备注',
    submit_time DATETIME COMMENT '提交时间',
    audit_time DATETIME COMMENT '审核时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人编号',
    update_by BIGINT COMMENT '更新人编号',
    deleted SMALLINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识'
) COMMENT='处方表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS pre_prescription_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号' PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '租户编号',
    prescription_id BIGINT NOT NULL COMMENT '处方编号',
    drug_id BIGINT NOT NULL COMMENT '药品编号',
    drug_name VARCHAR(128) NOT NULL COMMENT '药品名称',
    dosage VARCHAR(128) COMMENT '剂量',
    frequency VARCHAR(128) COMMENT '频次',
    quantity DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '数量',
    usage_note VARCHAR(512) COMMENT '用药备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人编号',
    update_by BIGINT COMMENT '更新人编号',
    deleted SMALLINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识'
) COMMENT='处方药品明细表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO pre_prescription (id, tenant_id, consult_id, patient_id, doctor_id, prescription_no, patient_name, doctor_name, drug_count, issued_at, status)
VALUES
    (1, 100, 1, 1, 1, 'CF20260612001', '赵晓岚', '陈知衡', 3, '09:42', '待审方'),
    (2, 100, 2, 2, 2, 'CF20260612002', '沈博远', '顾清和', 5, '09:18', '待发药'),
    (3, 100, 3, 3, 1, 'CF20260612003', '接口驳回患者', '陈知衡', 1, '09:50', '待审方')
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO pre_prescription_item (id, tenant_id, prescription_id, drug_id, drug_name, dosage, frequency, quantity, usage_note)
VALUES
    (1, 100, 1, 1, '阿托伐他汀钙片', '20mg', '每日一次', 1, '饭后服用'),
    (2, 100, 1, 2, '盐酸二甲双胍缓释片', '0.5g', '每日两次', 1, '随餐服用')
ON DUPLICATE KEY UPDATE id = id;

CREATE TABLE IF NOT EXISTS local_message (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号' PRIMARY KEY,
    topic VARCHAR(128) NOT NULL COMMENT '消息主题',
    body TEXT NOT NULL COMMENT '消息内容',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    max_retry INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    next_retry_time DATETIME COMMENT '下次重试时间',
    -- status: PENDING, SENT, FAILED
    status VARCHAR(16) NOT NULL COMMENT '消息状态',
    error_msg TEXT COMMENT '错误信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='本地消息表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

USE hospital_drug;

CREATE TABLE IF NOT EXISTS drug_info (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号' PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '租户编号',
    name VARCHAR(128) NOT NULL COMMENT '兼容旧表药品名称',
    drug_name VARCHAR(128) NOT NULL DEFAULT '' COMMENT '药品名称',
    spec VARCHAR(128) COMMENT '药品规格',
    manufacturer VARCHAR(128) COMMENT '生产厂家',
    unit VARCHAR(32) COMMENT '库存单位',
    inventory INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    warning_status VARCHAR(32) NOT NULL DEFAULT '正常' COMMENT '预警状态',
    price DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '药品价格',
    -- status: 0 disabled, 1 enabled
    status SMALLINT NOT NULL DEFAULT 1 COMMENT '药品状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人编号',
    update_by BIGINT COMMENT '更新人编号',
    deleted SMALLINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识'
) COMMENT='药品信息表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS drug_stock (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号' PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '租户编号',
    drug_id BIGINT NOT NULL COMMENT '药品编号',
    warehouse_name VARCHAR(64) NOT NULL DEFAULT '中心药房' COMMENT '仓库名称',
    inventory INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    warning_status VARCHAR(32) NOT NULL DEFAULT '正常' COMMENT '预警状态',
    stock_qty DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '库存数量兼容字段',
    locked_qty DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '锁定库存数量',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人编号',
    update_by BIGINT COMMENT '更新人编号',
    deleted SMALLINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识'
) COMMENT='药品库存表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS drug_delivery (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号' PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '租户编号',
    order_id BIGINT NOT NULL COMMENT '订单编号',
    prescription_id BIGINT NOT NULL COMMENT '处方编号',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '配送状态',
    receiver_name VARCHAR(64) NOT NULL COMMENT '收货人姓名',
    receiver_phone VARCHAR(32) NOT NULL COMMENT '收货人电话',
    receiver_address VARCHAR(512) NOT NULL COMMENT '收货地址',
    tracking_no VARCHAR(128) COMMENT '物流单号',
    ship_time DATETIME COMMENT '发货时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人编号',
    update_by BIGINT COMMENT '更新人编号',
    deleted SMALLINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识'
) COMMENT='药品配送表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

UPDATE drug_info SET drug_name = name WHERE drug_name = '' AND name <> '';

INSERT INTO drug_info (id, tenant_id, name, drug_name, spec, inventory, unit, warning_status, price, status)
VALUES
    (1, 100, '阿托伐他汀钙片', '阿托伐他汀钙片', '20mg*14片', 124, '盒', '正常', 23.80, 1),
    (2, 100, '盐酸二甲双胍缓释片', '盐酸二甲双胍缓释片', '0.5g*30片', 42, '盒', '预警', 18.60, 1)
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO drug_stock (id, tenant_id, drug_id, warehouse_name, inventory, warning_status, stock_qty)
VALUES
    (1, 100, 1, '中心药房', 124, '正常', 124),
    (2, 100, 2, '中心药房', 42, '预警', 42)
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO drug_delivery (id, tenant_id, order_id, prescription_id, status, receiver_name, receiver_phone, receiver_address, tracking_no)
VALUES
    (1, 100, 1, 1, 'PENDING', '赵晓岚', '13800000009', '杭州市西湖区互联网医院体验点', '')
ON DUPLICATE KEY UPDATE id = id;

CREATE TABLE IF NOT EXISTS local_message (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号' PRIMARY KEY,
    topic VARCHAR(128) NOT NULL COMMENT '消息主题',
    body TEXT NOT NULL COMMENT '消息内容',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    max_retry INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    next_retry_time DATETIME COMMENT '下次重试时间',
    -- status: PENDING, SENT, FAILED
    status VARCHAR(16) NOT NULL COMMENT '消息状态',
    error_msg TEXT COMMENT '错误信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='本地消息表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

USE hospital_order;

CREATE TABLE IF NOT EXISTS ord_order (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号' PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '租户编号',
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    -- biz_type: CONSULT, APPOINTMENT, PRESCRIPTION, DRUG
    biz_type VARCHAR(32) NOT NULL COMMENT '业务类型编码',
    biz_id BIGINT NOT NULL COMMENT '业务编号',
    patient_id BIGINT NOT NULL COMMENT '患者档案编号（关联pat_patient.id）',
    amount DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '订单金额',
    -- status: 0 pending pay, 1 paid, 2 closed, 3 refunded
    status SMALLINT NOT NULL DEFAULT 0 COMMENT '订单状态',
    pay_method VARCHAR(32) COMMENT '支付方式',
    pay_time DATETIME COMMENT '支付时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人编号',
    update_by BIGINT COMMENT '更新人编号',
    deleted SMALLINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识'
) COMMENT='订单表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO ord_order (id, tenant_id, order_no, biz_type, biz_id, patient_id, business_type, patient_name, amount, pay_status, created_at, status)
VALUES
    (1, 100, 'DD20260612001', 'APPOINTMENT', 1, 1, '门诊预约', '赵晓岚', 58.00, '已支付', '09:12', '已支付'),
    (2, 100, 'DD20260612002', 'CONSULT', 2, 2, '图文咨询', '沈博远', 39.90, '待支付', '09:35', '待支付')
ON DUPLICATE KEY UPDATE id = id;

CREATE TABLE IF NOT EXISTS local_message (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号' PRIMARY KEY,
    topic VARCHAR(128) NOT NULL COMMENT '消息主题',
    body TEXT NOT NULL COMMENT '消息内容',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    max_retry INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    next_retry_time DATETIME COMMENT '下次重试时间',
    -- status: PENDING, SENT, FAILED
    status VARCHAR(16) NOT NULL COMMENT '消息状态',
    error_msg TEXT COMMENT '错误信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='本地消息表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;

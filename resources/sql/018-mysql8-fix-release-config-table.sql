-- =============================================================
-- 018 — 修复 apt_number_source_release_config 表结构
-- 014 建表时列定义错误（doctor_id/release_date/time_slot/total_number
-- 应为 schedule_id/release_time/release_count）
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `hospital_appointment`;

-- 备份旧数据（如果有）
DROP TABLE IF EXISTS `apt_number_source_release_config_old`;
ALTER TABLE `apt_number_source_release_config` RENAME TO `apt_number_source_release_config_old`;

-- 重建正确结构的表
CREATE TABLE `apt_number_source_release_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键编号',
  `tenant_id` bigint NOT NULL COMMENT '租户编号',
  `schedule_id` bigint NOT NULL COMMENT '排班编号',
  `release_time` datetime DEFAULT NULL COMMENT '放号时间',
  `release_count` int NOT NULL DEFAULT '0' COMMENT '放号数量',
  `status` varchar(20) NOT NULL DEFAULT '启用' COMMENT '配置状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人编号',
  `update_by` bigint DEFAULT NULL COMMENT '更新人编号',
  `deleted` smallint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  PRIMARY KEY (`id`),
  KEY `idx_schedule_id` (`schedule_id`) COMMENT '排班编号索引'
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='放号配置表';

-- 丢弃旧表
DROP TABLE IF EXISTS `apt_number_source_release_config_old`;

SET FOREIGN_KEY_CHECKS = 1;

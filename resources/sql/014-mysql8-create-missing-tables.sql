-- =============================================================
-- 014 — 补齐所有缺失的表（appointment / order / prescription / drug）
-- 这些表的实体已存在，但 init.sql 中只有空数据库没有表定义
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ========================================
-- 数据库：hospital_appointment  — 预约相关表
-- ========================================
USE `hospital_appointment`;

-- ----------------------------
-- 预约单表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `apt_appointment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键编号',
  `tenant_id` bigint NOT NULL COMMENT '租户编号',
  `patient_id` bigint NOT NULL COMMENT '患者编号',
  `doctor_id` bigint DEFAULT NULL COMMENT '医生编号',
  `department_id` bigint DEFAULT NULL COMMENT '科室编号',
  `schedule_id` bigint DEFAULT NULL COMMENT '排班编号',
  `number_source_id` bigint DEFAULT NULL COMMENT '号源编号',
  `appointment_type` varchar(32) NOT NULL DEFAULT '' COMMENT '预约类型',
  `appointment_no` varchar(64) NOT NULL DEFAULT '' COMMENT '预约单号',
  `patient_name` varchar(100) NOT NULL DEFAULT '' COMMENT '患者姓名',
  `doctor_name` varchar(100) NOT NULL DEFAULT '' COMMENT '医生姓名',
  `clinic_time` varchar(64) NOT NULL DEFAULT '' COMMENT '就诊时间',
  `source` varchar(32) NOT NULL DEFAULT '' COMMENT '预约来源',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING_PAY' COMMENT '预约状态',
  `fee_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '预约费用',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `check_in_time` datetime DEFAULT NULL COMMENT '签到时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人编号',
  `update_by` bigint DEFAULT NULL COMMENT '更新人编号',
  `deleted` smallint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预约单表';

-- ----------------------------
-- 号源表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `apt_number_source` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键编号',
  `tenant_id` bigint NOT NULL COMMENT '租户编号',
  `schedule_id` bigint NOT NULL COMMENT '排班编号',
  `number_seq` int NOT NULL COMMENT '号源序号',
  `status` varchar(20) NOT NULL DEFAULT 'AVAILABLE' COMMENT '号源状态（AVAILABLE/LOCKED/USED）',
  `lock_time` datetime DEFAULT NULL COMMENT '锁定时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人编号',
  `update_by` bigint DEFAULT NULL COMMENT '更新人编号',
  `deleted` smallint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  PRIMARY KEY (`id`),
  KEY `idx_schedule_id` (`schedule_id`) COMMENT '排班编号索引'
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='号源表';

-- ----------------------------
-- 放号配置表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `apt_number_source_release_config` (
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


-- ========================================
-- 数据库：hospital_order  — 订单表
-- ========================================
USE `hospital_order`;

-- ----------------------------
-- 订单表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `ord_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键编号',
  `tenant_id` bigint NOT NULL COMMENT '租户编号',
  `order_no` varchar(64) NOT NULL DEFAULT '' COMMENT '订单号',
  `biz_type` varchar(32) NOT NULL DEFAULT '' COMMENT '业务类型编码',
  `biz_id` bigint DEFAULT NULL COMMENT '业务编号',
  `patient_id` bigint DEFAULT NULL COMMENT '患者编号',
  `business_type` varchar(32) DEFAULT NULL COMMENT '业务类型',
  `patient_name` varchar(100) NOT NULL DEFAULT '' COMMENT '患者姓名',
  `amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '订单金额',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '订单状态',
  `pay_status` varchar(20) NOT NULL DEFAULT 'UNPAID' COMMENT '支付状态',
  `pay_method` varchar(32) DEFAULT NULL COMMENT '支付方式',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `created_at` varchar(32) DEFAULT NULL COMMENT '订单创建时间展示值',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人编号',
  `update_by` bigint DEFAULT NULL COMMENT '更新人编号',
  `deleted` smallint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单表';


-- ========================================
-- 数据库：hospital_prescription  — 处方表
-- ========================================
USE `hospital_prescription`;

-- ----------------------------
-- 处方表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `pre_prescription` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键编号',
  `tenant_id` bigint NOT NULL COMMENT '租户编号',
  `prescription_no` varchar(64) NOT NULL DEFAULT '' COMMENT '处方号',
  `patient_id` bigint DEFAULT NULL COMMENT '患者编号',
  `doctor_id` bigint DEFAULT NULL COMMENT '医生编号',
  `consult_id` bigint DEFAULT NULL COMMENT '问诊编号',
  `patient_name` varchar(100) NOT NULL DEFAULT '' COMMENT '患者姓名',
  `doctor_name` varchar(100) NOT NULL DEFAULT '' COMMENT '医生姓名',
  `drug_count` int NOT NULL DEFAULT '0' COMMENT '药品数量',
  `total_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '总金额',
  `status` varchar(20) NOT NULL DEFAULT 'DRAFT' COMMENT '处方状态',
  `diagnosis` text COMMENT '诊断信息',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `issued_at` datetime DEFAULT NULL COMMENT '开具时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人编号',
  `update_by` bigint DEFAULT NULL COMMENT '更新人编号',
  `deleted` smallint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='处方表';

-- ----------------------------
-- 处方明细表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `pre_prescription_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键编号',
  `tenant_id` bigint NOT NULL COMMENT '租户编号',
  `prescription_id` bigint NOT NULL COMMENT '处方编号',
  `drug_id` bigint DEFAULT NULL COMMENT '药品编号',
  `drug_name` varchar(200) NOT NULL DEFAULT '' COMMENT '药品名称',
  `spec` varchar(200) DEFAULT NULL COMMENT '规格',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '数量',
  `days` int DEFAULT NULL COMMENT '天数',
  `usage_method` varchar(200) DEFAULT NULL COMMENT '用法',
  `frequency` varchar(100) DEFAULT NULL COMMENT '频率',
  `amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '金额',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人编号',
  `update_by` bigint DEFAULT NULL COMMENT '更新人编号',
  `deleted` smallint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  PRIMARY KEY (`id`),
  KEY `idx_prescription_id` (`prescription_id`) COMMENT '处方编号索引'
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='处方明细表';


-- ========================================
-- 数据库：hospital_drug  — 药品表
-- ========================================
USE `hospital_drug`;

-- ----------------------------
-- 药品信息表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `drug_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键编号',
  `tenant_id` bigint NOT NULL COMMENT '租户编号',
  `drug_name` varchar(200) NOT NULL COMMENT '药品名称',
  `spec` varchar(200) DEFAULT NULL COMMENT '规格',
  `manufacturer` varchar(200) DEFAULT NULL COMMENT '生产厂家',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '单价',
  `category` varchar(50) DEFAULT NULL COMMENT '药品分类',
  `approval_number` varchar(100) DEFAULT NULL COMMENT '批准文号',
  `status` varchar(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人编号',
  `update_by` bigint DEFAULT NULL COMMENT '更新人编号',
  `deleted` smallint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='药品信息表';

-- ----------------------------
-- 药品库存表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `drug_stock` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键编号',
  `tenant_id` bigint NOT NULL COMMENT '租户编号',
  `drug_id` bigint NOT NULL COMMENT '药品编号',
  `warehouse_name` varchar(100) NOT NULL DEFAULT '' COMMENT '仓库名称',
  `inventory` int NOT NULL DEFAULT '0' COMMENT '库存数量',
  `warning_status` varchar(20) DEFAULT NULL COMMENT '预警状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人编号',
  `update_by` bigint DEFAULT NULL COMMENT '更新人编号',
  `deleted` smallint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  PRIMARY KEY (`id`),
  KEY `idx_drug_id` (`drug_id`) COMMENT '药品编号索引'
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='药品库存表';

-- ----------------------------
-- 药品配送表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `drug_delivery` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键编号',
  `tenant_id` bigint NOT NULL COMMENT '租户编号',
  `order_id` bigint DEFAULT NULL COMMENT '订单编号',
  `prescription_id` bigint DEFAULT NULL COMMENT '处方编号',
  `patient_name` varchar(100) NOT NULL DEFAULT '' COMMENT '患者姓名',
  `address` varchar(500) DEFAULT NULL COMMENT '配送地址',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '配送状态',
  `delivery_time` datetime DEFAULT NULL COMMENT '配送时间',
  `receive_time` datetime DEFAULT NULL COMMENT '签收时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人编号',
  `update_by` bigint DEFAULT NULL COMMENT '更新人编号',
  `deleted` smallint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='药品配送表';

SET FOREIGN_KEY_CHECKS = 1;

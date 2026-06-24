-- =============================================================
-- 020 — 为挂号（apt_appointment）、问诊（con_consult）、
--        订单（ord_order）插入演示种子数据
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ========================================
-- 数据库：hospital_appointment
-- ========================================
USE `hospital_appointment`;

-- 清空已有预约单（幂等）
DELETE FROM `apt_appointment`;

-- 插入预约单演示数据
INSERT INTO `apt_appointment` (`tenant_id`, `patient_id`, `doctor_id`, `department_id`, `schedule_id`, `number_source_id`, `appointment_type`, `appointment_no`, `patient_name`, `doctor_name`, `clinic_time`, `source`, `status`, `fee_amount`, `pay_time`, `check_in_time`, `create_time`, `update_time`, `deleted`)
VALUES
    -- 租户 100，患者 赵晓岚，医生 陈知衡
    (100, 1, 1, 10, 1, 1, '普通门诊', 'YY202606250001', '赵晓岚', '陈知衡', '2026-06-25 上午', 'PATIENT_H5', '待支付', 30.00, NULL, NULL, NOW(), NOW(), 0),
    (100, 1, 1, 10, 1, 2, '普通门诊', 'YY202606250002', '赵晓岚', '陈知衡', '2026-06-25 下午', 'PATIENT_H5', '已支付', 30.00, NOW(), NULL, NOW(), NOW(), 0),
    (100, 1, 1, 10, 1, 3, '普通门诊', 'YY202606250003', '赵晓岚', '陈知衡', '2026-06-26 上午', 'PATIENT_H5', '已签到', 30.00, NOW(), NOW(), NOW(), NOW(), 0),
    (100, 1, 1, 10, 1, 4, '普通门诊', 'YY202606250004', '赵晓岚', '陈知衡', '2026-06-26 下午', 'PATIENT_H5', '已完成', 30.00, NOW(), NOW(), NOW(), NOW(), 0),
    -- 租户 100，患者 沈博远，医生 顾清和
    (100, 2, 2, 20, 1, 5, '普通门诊', 'YY202606250005', '沈博远', '顾清和', '2026-06-27 上午', 'PATIENT_H5', '待支付', 20.00, NULL, NULL, NOW(), NOW(), 0),
    -- 租户 17822850632175495，患者 11，医生 李志
    (17822850632175495, 11, 4, 14, 2, 6, '普通门诊', 'YY202606250006', '赵晓岚', '李志', '2026-06-25 上午', 'PATIENT_H5', '待支付', 50.00, NULL, NULL, NOW(), NOW(), 0),
    (17822850632175495, 11, 4, 14, 2, 7, '普通门诊', 'YY202606250007', '赵晓岚', '李志', '2026-06-25 下午', 'PATIENT_H5', '已支付', 50.00, NOW(), NULL, NOW(), NOW(), 0);


-- ========================================
-- 数据库：hospital_consult
-- ========================================
USE `hospital_consult`;

-- 为租户 100 补充问诊演示数据（已有关联预约单的）
INSERT IGNORE INTO `con_consult` (`id`, `tenant_id`, `patient_id`, `doctor_id`, `appointment_id`, `patient_name`, `doctor_name`, `channel`, `consult_type`, `consult_no`, `status`, `pay_status`, `fee_amount`, `duration_limit`, `remaining_seconds`, `updated_at`, `create_time`, `update_time`, `deleted`)
VALUES
    -- 从预约单 YY202606250002（已支付，fee=30.00）创建的已完成问诊
    (100, 100, 1, 1, 2, '赵晓岚', '陈知衡', '图文', 'text_and_image_consultation', 'ZX202606250001', 3, 'PAID', 30.00, 30, 0, '10:30', NOW(), NOW(), 0),
    -- 从预约单 YY202606250004（已完成，fee=30.00）创建的已完成问诊
    (101, 100, 1, 1, 4, '赵晓岚', '陈知衡', '图文', 'text_and_image_consultation', 'ZX202606250002', 3, 'PAID', 30.00, 30, 0, '14:00', NOW(), NOW(), 0);


-- ========================================
-- 数据库：hospital_order
-- ========================================
USE `hospital_order`;

-- 清空已有订单（幂等）
DELETE FROM `ord_order`;

-- 插入订单演示数据
INSERT INTO `ord_order` (`tenant_id`, `order_no`, `biz_type`, `biz_id`, `patient_id`, `business_type`, `patient_name`, `amount`, `status`, `pay_status`, `pay_method`, `pay_time`, `created_at`, `create_time`, `update_time`, `deleted`)
VALUES
    -- 租户 100，赵晓岚
    (100, 'DD202606250001', 'APPOINTMENT', 1, 1, '门诊预约', '赵晓岚', 30.00, '待支付', '待支付', NULL, NULL, '10:00', NOW(), NOW(), 0),
    (100, 'DD202606250002', 'APPOINTMENT', 2, 1, '门诊预约', '赵晓岚', 30.00, '已支付', '已支付', 'MOCK_PAY', NOW(), '10:05', NOW(), NOW(), 0),
    (100, 'DD202606250003', 'CONSULT', 100, 1, '图文咨询', '赵晓岚', 39.90, '已支付', '已支付', 'MOCK_PAY', NOW(), '10:10', NOW(), NOW(), 0),
    -- 租户 17822850632175495，患者 11
    (17822850632175495, 'DD202606250004', 'APPOINTMENT', 6, 11, '门诊预约', '赵晓岚', 50.00, '待支付', '待支付', NULL, NULL, '09:00', NOW(), NOW(), 0),
    (17822850632175495, 'DD202606250005', 'APPOINTMENT', 7, 11, '门诊预约', '赵晓岚', 50.00, '已支付', '已支付', 'MOCK_PAY', NOW(), '09:30', NOW(), NOW(), 0);

SET FOREIGN_KEY_CHECKS = 1;

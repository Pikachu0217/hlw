-- =============================================================
-- 022 — 补充排班与预约防重索引
-- 排班限制同一医生同一日期时间段只能存在一条有效记录；
-- 预约按患者、医生、科室、门诊时间组合提升防重查询性能。
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `hospital_doctor`;

ALTER TABLE `doc_schedule`
    ADD UNIQUE KEY `uk_doc_schedule_doctor_time`
        (`tenant_id`, `doctor_id`, `schedule_date`, `time_slot`, `deleted`) COMMENT '租户医生日期时间段唯一索引';

USE `hospital_appointment`;

ALTER TABLE `apt_appointment`
    ADD INDEX `idx_patient_doctor_dept_time_status`
        (`tenant_id`, `patient_id`, `doctor_id`, `department_id`, `clinic_time`, `status`) COMMENT '患者医生科室门诊时间状态索引';

SET FOREIGN_KEY_CHECKS = 1;

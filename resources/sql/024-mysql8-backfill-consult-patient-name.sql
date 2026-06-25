-- =============================================================
-- 024 — 回填历史问诊患者姓名
-- 从患者档案补齐 con_consult.patient_name；患者档案姓名也为空时，
-- 使用“患者 + 患者编号”作为展示兜底，避免咨询列表只显示问诊单号。
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `hospital_consult`;

UPDATE `con_consult` c
LEFT JOIN `hospital_patient`.`pat_patient` p
    ON p.`tenant_id` = c.`tenant_id`
    AND p.`id` = c.`patient_id`
    AND p.`deleted` = 0
SET c.`patient_name` = COALESCE(
    NULLIF(p.`patient_name`, ''),
    NULLIF(p.`name`, ''),
    CONCAT('患者', c.`patient_id`)
)
WHERE c.`deleted` = 0
  AND (c.`patient_name` IS NULL OR c.`patient_name` = '');

SET FOREIGN_KEY_CHECKS = 1;

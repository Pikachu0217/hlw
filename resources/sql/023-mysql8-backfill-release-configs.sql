-- =============================================================
-- 023 — 补齐历史排班放号配置
-- 仅为缺少放号配置的历史排班补齐 apt_number_source_release_config；
-- 不向 apt_number_source 预生成号源，号源仍在患者占号后按需写入。
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `hospital_appointment`;

INSERT INTO `apt_number_source_release_config`
    (`tenant_id`, `schedule_id`, `release_time`, `release_count`, `status`, `create_time`, `update_time`, `deleted`)
SELECT
    s.`tenant_id`,
    s.`id`,
    NOW(),
    GREATEST(COALESCE(s.`total_number`, 0), 0),
    '启用',
    NOW(),
    NOW(),
    0
FROM `hospital_doctor`.`doc_schedule` s
WHERE s.`deleted` = 0
  AND COALESCE(s.`total_number`, 0) > 0
  AND NOT EXISTS (
      SELECT 1
      FROM `apt_number_source_release_config` rc
      WHERE rc.`tenant_id` = s.`tenant_id`
        AND rc.`schedule_id` = s.`id`
        AND rc.`deleted` = 0
      LIMIT 1
  );

SET FOREIGN_KEY_CHECKS = 1;

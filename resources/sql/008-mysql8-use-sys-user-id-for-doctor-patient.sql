-- 医生与患者档案关联 sys_user.id 迁移脚本。
-- 执行方式示例：mysql -u root -p < resources/sql/008-mysql8-use-sys-user-id-for-doctor-patient.sql
-- 说明：若旧库 user_id 存储的是 sys_user.user_id 字符串，会优先映射回 sys_user.id 数字主键；未匹配的数据会保留可转换的数字值，否则置为 0。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP PROCEDURE IF EXISTS migrate_doctor_patient_account_user_id;

DELIMITER $$
CREATE PROCEDURE migrate_doctor_patient_account_user_id()
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'hospital_patient'
          AND table_name = 'pat_patient'
    ) THEN
        DROP TEMPORARY TABLE IF EXISTS `tmp_patient_user_mapping`;
        CREATE TEMPORARY TABLE `tmp_patient_user_mapping` AS
        SELECT patient.`id` AS `patient_id`,
               COALESCE(sys_user.`id`, CASE WHEN CAST(patient.`user_id` AS CHAR) REGEXP '^[0-9]+$' THEN CAST(patient.`user_id` AS UNSIGNED) ELSE 0 END) AS `account_user_id`
        FROM `hospital_patient`.`pat_patient` patient
        LEFT JOIN `hospital_system`.`sys_user` sys_user
          ON sys_user.`tenant_id` = CAST(patient.`tenant_id` AS CHAR)
         AND sys_user.`user_id` = CAST(patient.`user_id` AS CHAR)
         AND sys_user.`deleted` = 0;

        UPDATE `hospital_patient`.`pat_patient` patient
        JOIN `tmp_patient_user_mapping` mapping
          ON mapping.`patient_id` = patient.`id`
        SET patient.`user_id` = mapping.`account_user_id`;

        ALTER TABLE `hospital_patient`.`pat_patient`
            MODIFY COLUMN `user_id` bigint NOT NULL COMMENT '关联用户编号（关联sys_user.id）';

        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = 'hospital_patient'
              AND table_name = 'pat_patient'
              AND index_name = 'idx_pat_patient_user'
        ) THEN
            ALTER TABLE `hospital_patient`.`pat_patient`
                ADD KEY `idx_pat_patient_user` (`tenant_id`, `user_id`);
        END IF;

        DROP TEMPORARY TABLE IF EXISTS `tmp_patient_user_mapping`;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'hospital_doctor'
          AND table_name = 'doc_doctor'
    ) THEN
        DROP TEMPORARY TABLE IF EXISTS `tmp_doctor_user_mapping`;
        CREATE TEMPORARY TABLE `tmp_doctor_user_mapping` AS
        SELECT doctor.`id` AS `doctor_id`,
               COALESCE(sys_user.`id`, CASE WHEN CAST(doctor.`user_id` AS CHAR) REGEXP '^[0-9]+$' THEN CAST(doctor.`user_id` AS UNSIGNED) ELSE 0 END) AS `account_user_id`
        FROM `hospital_doctor`.`doc_doctor` doctor
        LEFT JOIN `hospital_system`.`sys_user` sys_user
          ON sys_user.`tenant_id` = CAST(doctor.`tenant_id` AS CHAR)
         AND sys_user.`user_id` = CAST(doctor.`user_id` AS CHAR)
         AND sys_user.`deleted` = 0;

        UPDATE `hospital_doctor`.`doc_doctor` doctor
        JOIN `tmp_doctor_user_mapping` mapping
          ON mapping.`doctor_id` = doctor.`id`
        SET doctor.`user_id` = mapping.`account_user_id`;

        ALTER TABLE `hospital_doctor`.`doc_doctor`
            MODIFY COLUMN `user_id` bigint NOT NULL COMMENT '关联用户编号（关联sys_user.id）';

        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = 'hospital_doctor'
              AND table_name = 'doc_doctor'
              AND index_name = 'idx_doc_doctor_user'
        ) THEN
            ALTER TABLE `hospital_doctor`.`doc_doctor`
                ADD KEY `idx_doc_doctor_user` (`tenant_id`, `user_id`);
        END IF;

        DROP TEMPORARY TABLE IF EXISTS `tmp_doctor_user_mapping`;
    END IF;
END$$
DELIMITER ;

CALL migrate_doctor_patient_account_user_id();

DROP PROCEDURE IF EXISTS migrate_doctor_patient_account_user_id;

SET FOREIGN_KEY_CHECKS = 1;

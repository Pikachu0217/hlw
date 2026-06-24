-- 修复医生与患者档案用户关联字段为 sys_user.user_id 字符串。
-- 执行方式示例：mysql -u root -p < resources/sql/011-mysql8-fix-doctor-patient-user-id-string.sql
-- 说明：用于已执行过旧版 008 脚本的数据库，将 doc_doctor.user_id、pat_patient.user_id 从数字主键值恢复为用户业务编号。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP PROCEDURE IF EXISTS fix_doctor_patient_business_user_id;

DELIMITER $$
CREATE PROCEDURE fix_doctor_patient_business_user_id()
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'hospital_patient'
          AND table_name = 'pat_patient'
    ) THEN
        ALTER TABLE `hospital_patient`.`pat_patient`
            MODIFY COLUMN `user_id` varchar(34) NOT NULL COMMENT '关联用户编号（关联sys_user.user_id）';

        UPDATE `hospital_patient`.`pat_patient` patient
        LEFT JOIN `hospital_system`.`sys_user` user_by_business
          ON user_by_business.`tenant_id` = CAST(patient.`tenant_id` AS CHAR)
         AND user_by_business.`user_id` = patient.`user_id`
         AND user_by_business.`deleted` = 0
        LEFT JOIN `hospital_system`.`sys_user` user_by_pk
          ON user_by_pk.`tenant_id` = CAST(patient.`tenant_id` AS CHAR)
         AND CAST(user_by_pk.`id` AS CHAR) = patient.`user_id`
         AND user_by_pk.`deleted` = 0
        SET patient.`user_id` = COALESCE(user_by_business.`user_id`, user_by_pk.`user_id`, patient.`user_id`);

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
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'hospital_doctor'
          AND table_name = 'doc_doctor'
    ) THEN
        ALTER TABLE `hospital_doctor`.`doc_doctor`
            MODIFY COLUMN `user_id` varchar(34) NOT NULL COMMENT '关联用户编号（关联sys_user.user_id）';

        UPDATE `hospital_doctor`.`doc_doctor` doctor
        LEFT JOIN `hospital_system`.`sys_user` user_by_business
          ON user_by_business.`tenant_id` = CAST(doctor.`tenant_id` AS CHAR)
         AND user_by_business.`user_id` = doctor.`user_id`
         AND user_by_business.`deleted` = 0
        LEFT JOIN `hospital_system`.`sys_user` user_by_pk
          ON user_by_pk.`tenant_id` = CAST(doctor.`tenant_id` AS CHAR)
         AND CAST(user_by_pk.`id` AS CHAR) = doctor.`user_id`
         AND user_by_pk.`deleted` = 0
        SET doctor.`user_id` = COALESCE(user_by_business.`user_id`, user_by_pk.`user_id`, doctor.`user_id`);

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
    END IF;
END$$
DELIMITER ;

CALL fix_doctor_patient_business_user_id();

DROP PROCEDURE IF EXISTS fix_doctor_patient_business_user_id;

SET FOREIGN_KEY_CHECKS = 1;

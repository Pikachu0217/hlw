-- =============================================================
-- 021 — apt_number_source 补充查询与清理索引，适配按需生成号源
-- 占号时动态 INSERT 替代预生成，核心查询路径需要复合索引支撑
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `hospital_appointment`;

-- 1) 按排班+状态查询：占号时校验容量（schedule_id + status IN LOCKED/USED）及锁定时的 INSERT 竞争
ALTER TABLE `apt_number_source`
    ADD INDEX `idx_schedule_status` (`schedule_id`, `status`) COMMENT '排班编号+状态复合索引';

-- 2) 按状态统计/扫描：后续清理或统计已用号源
ALTER TABLE `apt_number_source`
    ADD INDEX `idx_status` (`status`) COMMENT '号源状态单列索引';

-- 3) 按更新时间清理：后续定时归档 USED 记录
ALTER TABLE `apt_number_source`
    ADD INDEX `idx_update_time` (`update_time`) COMMENT '更新时间单列索引';

SET FOREIGN_KEY_CHECKS = 1;

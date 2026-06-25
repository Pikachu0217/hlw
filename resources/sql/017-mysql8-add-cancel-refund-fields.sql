-- 互联网医院 MySQL 8 增量脚本：补充预约单和订单表的取消/退款字段。
-- 编号：017
-- 依赖：所有 001-016 脚本或全量 init.sql 已执行。

-- ========================================
-- 预约单表：补充取消时间和原因
-- ========================================
USE `hospital_appointment`;

ALTER TABLE `apt_appointment`
    ADD COLUMN `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间'
    AFTER `check_in_time`;

ALTER TABLE `apt_appointment`
    ADD COLUMN `cancel_reason` VARCHAR(256) NOT NULL DEFAULT '' COMMENT '取消原因'
    AFTER `cancel_time`;

-- ========================================
-- 订单表：补充退款时间和金额
-- ========================================
USE `hospital_order`;

ALTER TABLE `ord_order`
    ADD COLUMN `refund_time` DATETIME DEFAULT NULL COMMENT '退款时间'
    AFTER `pay_time`;

ALTER TABLE `ord_order`
    ADD COLUMN `refund_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '退款金额'
    AFTER `refund_time`;

-- ========================================
-- 号源表：补充释放时间
-- ========================================
USE `hospital_appointment`;

ALTER TABLE `apt_number_source`
    ADD COLUMN `release_time` DATETIME DEFAULT NULL COMMENT '释放时间'
    AFTER `lock_time`;

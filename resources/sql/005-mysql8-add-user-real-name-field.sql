-- 为 sys_user 表添加真实姓名字段，字段顺序位于 user_name 后面

USE hospital_system;

ALTER TABLE `sys_user` ADD COLUMN `real_name` varchar(30) NOT NULL DEFAULT '' COMMENT '真实姓名' AFTER `user_name`;

UPDATE `sys_user`
SET `real_name` = CASE
    WHEN `nick_name` IS NULL OR `nick_name` = '' THEN `user_name`
    ELSE `nick_name`
END
WHERE `real_name` = '';

INSERT INTO `sys_dict_type` (`tenant_id`, `dict_name`, `dict_type`, `remark`, `deleted`)
VALUES ('0', '用户类型', 'user_type', '后台账号用户类型', 0)
ON DUPLICATE KEY UPDATE `dict_name` = VALUES(`dict_name`),
                        `remark` = VALUES(`remark`),
                        `deleted` = VALUES(`deleted`);

INSERT INTO `sys_dict_data` (`tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `remark`, `deleted`)
VALUES
    ('0', 1, '系统用户', 'sys_user', 'user_type', '后台系统用户', 0),
    ('0', 2, '医生', 'doctor', 'user_type', '医生工作台用户', 0),
    ('0', 3, '药师', 'pharmacist', 'user_type', '药师工作台用户', 0)
ON DUPLICATE KEY UPDATE `dict_sort` = VALUES(`dict_sort`),
                        `dict_label` = VALUES(`dict_label`),
                        `remark` = VALUES(`remark`),
                        `deleted` = VALUES(`deleted`);

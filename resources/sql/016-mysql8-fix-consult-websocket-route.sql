USE hospital_gateway;

UPDATE `gw_route_config`
SET `path_predicate` = '/consult/**',
    `remark` = '问诊服务 HTTP 路由',
    `update_time` = CURRENT_TIMESTAMP
WHERE `route_code` = 'hospital-consult'
  AND `deleted` = 0;

INSERT INTO `gw_route_config`
    (`tenant_id`, `route_code`, `uri`, `path_predicate`, `sort`, `status`, `remark`, `create_time`, `update_time`, `deleted`)
VALUES
    (0, 'hospital-consult-ws', 'lb:ws://hospital-consult', '/ws/consult/**', 61, '0', '问诊服务 WebSocket 路由', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON DUPLICATE KEY UPDATE
    `uri` = VALUES(`uri`),
    `path_predicate` = VALUES(`path_predicate`),
    `sort` = VALUES(`sort`),
    `status` = VALUES(`status`),
    `remark` = VALUES(`remark`),
    `update_time` = CURRENT_TIMESTAMP,
    `deleted` = 0;

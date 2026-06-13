package com.hlw.system.controller;

import com.hlw.common.core.domain.R;
import com.hlw.common.core.jdbc.DemoDataQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system")
public class SystemManagementController {
    private static final Logger log = LoggerFactory.getLogger(SystemManagementController.class);

    private final DemoDataQuery demoDataQuery;

    /**
     * 构造系统管理控制器。
     *
     * @param demoDataQuery 演示数据查询器
     */
    public SystemManagementController(DemoDataQuery demoDataQuery) {
        this.demoDataQuery = demoDataQuery;
    }

    /**
     * 查询租户列表。
     *
     * @return 租户列表
     */
    @GetMapping("/tenants")
    public R<List<Map<String, Object>>> tenants() {
        log.info("查询租户列表");
        return R.ok(demoDataQuery.list("租户列表", """
            SELECT id::text AS key,
                   tenant_name AS "tenantName",
                   package_name AS "packageName",
                   admin_name AS "adminName",
                   to_char(expire_at, 'YYYY-MM-DD') AS "expireAt",
                   status AS status
            FROM sys_tenant
            WHERE deleted = 0
            ORDER BY id
            """));
    }

    /**
     * 创建租户。
     *
     * @return 创建结果
     */
    @PostMapping("/tenants")
    public R<Map<String, Object>> createTenant() {
        return R.ok(Map.of());
    }

    /**
     * 查询后台用户列表。
     *
     * @return 后台用户列表
     */
    @GetMapping("/users")
    public R<List<Map<String, Object>>> users() {
        log.info("查询后台用户列表");
        return R.ok(demoDataQuery.list("后台用户列表", """
            SELECT id::text AS key,
                   username AS username,
                   dept_name AS "deptName",
                   role_name AS "roleName",
                   phone AS phone,
                   last_login AS "lastLogin",
                   status AS status
            FROM sys_user
            WHERE deleted = 0
            ORDER BY id
            """));
    }

    /**
     * 查询角色列表。
     *
     * @return 角色列表
     */
    @GetMapping("/roles")
    public R<List<Map<String, Object>>> roles() {
        log.info("查询角色列表");
        return R.ok(demoDataQuery.list("角色列表", """
            SELECT id::text AS key,
                   role_name AS "roleName",
                   data_scope AS "dataScope",
                   member_count AS "memberCount",
                   to_char(update_time, 'YYYY-MM-DD HH24:MI') AS "updatedAt",
                   status AS status
            FROM sys_role
            WHERE deleted = 0
            ORDER BY id
            """));
    }

    /**
     * 查询菜单列表。
     *
     * @return 菜单列表
     */
    @GetMapping("/menus")
    public R<List<Map<String, Object>>> menus() {
        log.info("查询菜单列表");
        return R.ok(demoDataQuery.list("菜单列表", """
            SELECT id::text AS key,
                   menu_name AS "menuName",
                   menu_type AS "menuType",
                   permission AS permission,
                   route_path AS "routePath",
                   status AS status
            FROM sys_menu
            WHERE deleted = 0
            ORDER BY id
            """));
    }
}

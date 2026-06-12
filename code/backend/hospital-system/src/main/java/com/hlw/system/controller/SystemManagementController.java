package com.hlw.system.controller;

import com.hlw.common.core.domain.R;
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

    /**
     * 查询租户列表。
     *
     * @return 租户列表
     */
    @GetMapping("/tenants")
    public R<List<Map<String, Object>>> tenants() {
        log.info("查询租户列表");
        return R.ok(List.of(
            Map.of("key", "1", "tenantName", "海岚门诊", "packageName", "标准医疗版", "adminName", "刘院长", "expireAt", "2026-12-31", "status", "正常"),
            Map.of("key", "2", "tenantName", "青禾互联网医院", "packageName", "集团旗舰版", "adminName", "姜主任", "expireAt", "2026-08-16", "status", "续费跟进")
        ));
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
        return R.ok(List.of(
            Map.of("key", "1", "username", "门诊运营", "deptName", "运营中心", "roleName", "运营管理员", "phone", "13800001111", "lastLogin", "今天 08:40", "status", "启用"),
            Map.of("key", "2", "username", "药房主管", "deptName", "药房组", "roleName", "库存专员", "phone", "13800002222", "lastLogin", "今天 07:58", "status", "启用")
        ));
    }

    /**
     * 查询角色列表。
     *
     * @return 角色列表
     */
    @GetMapping("/roles")
    public R<List<Map<String, Object>>> roles() {
        log.info("查询角色列表");
        return R.ok(List.of(
            Map.of("key", "1", "roleName", "系统管理员", "dataScope", "全部数据", "memberCount", 3, "updatedAt", "2026-06-10 11:20", "status", "启用"),
            Map.of("key", "2", "roleName", "运营管理员", "dataScope", "本租户数据", "memberCount", 11, "updatedAt", "2026-06-09 17:45", "status", "启用")
        ));
    }

    /**
     * 查询菜单列表。
     *
     * @return 菜单列表
     */
    @GetMapping("/menus")
    public R<List<Map<String, Object>>> menus() {
        log.info("查询菜单列表");
        return R.ok(List.of(
            Map.of("key", "1", "menuName", "工作台", "menuType", "菜单", "permission", "dashboard:view", "routePath", "/dashboard", "status", "启用"),
            Map.of("key", "2", "menuName", "医生管理", "menuType", "菜单", "permission", "doctor:list", "routePath", "/doctor", "status", "启用")
        ));
    }
}

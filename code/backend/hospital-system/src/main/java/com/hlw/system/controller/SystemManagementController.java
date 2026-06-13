package com.hlw.system.controller;

import com.hlw.common.core.domain.R;
import com.hlw.common.core.jdbc.DemoDataQuery;
import com.hlw.system.service.SystemCatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system")
public class SystemManagementController {
    private static final Logger log = LoggerFactory.getLogger(SystemManagementController.class);

    private final DemoDataQuery demoDataQuery;
    private final SystemCatalogService systemCatalogService;

    /**
     * 构造系统管理控制器。
     *
     * @param demoDataQuery 演示数据查询器
     * @param systemCatalogService 系统基础管理服务
     */
    public SystemManagementController(DemoDataQuery demoDataQuery, SystemCatalogService systemCatalogService) {
        this.demoDataQuery = demoDataQuery;
        this.systemCatalogService = systemCatalogService;
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
    public R<Map<String, Object>> createTenant(@RequestBody Map<String, Object> command) {
        log.info("创建租户，command={}", command);
        return R.ok(command);
    }

    /**
     * 查询后台用户列表。
     *
     * @return 后台用户列表
     */
    @GetMapping("/users")
    public R<List<Map<String, Object>>> users() {
        return R.ok(systemCatalogService.listUsers());
    }

    /**
     * 查询角色列表。
     *
     * @return 角色列表
     */
    @GetMapping("/roles")
    public R<List<Map<String, Object>>> roles() {
        return R.ok(systemCatalogService.listRoles());
    }

    /**
     * 查询菜单列表。
     *
     * @return 菜单列表
     */
    @GetMapping("/menus")
    public R<List<Map<String, Object>>> menus() {
        return R.ok(systemCatalogService.listMenus());
    }

    /**
     * 查询字典列表。
     *
     * @return 字典列表
     */
    @GetMapping("/dicts")
    public R<List<Map<String, Object>>> dicts() {
        return R.ok(systemCatalogService.listDicts());
    }

    /**
     * 创建字典项。
     *
     * @param command 字典创建命令
     * @return 创建后的字典项
     */
    @PostMapping("/dicts")
    public R<Map<String, Object>> createDict(@RequestBody Map<String, Object> command) {
        return R.ok(systemCatalogService.createDict(command));
    }

    /**
     * 查询系统参数配置列表。
     *
     * @return 系统参数配置列表
     */
    @GetMapping("/configs")
    public R<List<Map<String, Object>>> configs() {
        return R.ok(systemCatalogService.listConfigs());
    }

    /**
     * 更新系统参数配置。
     *
     * @param id 配置编号
     * @param command 配置更新命令
     * @return 更新后的配置
     */
    @PutMapping("/configs/{id}")
    public R<Map<String, Object>> updateConfig(@PathVariable Long id, @RequestBody Map<String, Object> command) {
        return R.ok(systemCatalogService.updateConfig(id, command));
    }

    /**
     * 查询岗位列表。
     *
     * @return 岗位列表
     */
    @GetMapping("/posts")
    public R<List<Map<String, Object>>> posts() {
        return R.ok(systemCatalogService.listPosts());
    }

    /**
     * 创建岗位。
     *
     * @param command 岗位创建命令
     * @return 创建后的岗位
     */
    @PostMapping("/posts")
    public R<Map<String, Object>> createPost(@RequestBody Map<String, Object> command) {
        return R.ok(systemCatalogService.createPost(command));
    }

    /**
     * 查询权限码列表。
     *
     * @return 权限码列表
     */
    @GetMapping("/permissions")
    public R<List<Map<String, Object>>> permissions() {
        return R.ok(systemCatalogService.listPermissions());
    }

    /**
     * 查询用户角色授权列表。
     *
     * @return 用户角色授权列表
     */
    @GetMapping("/user-roles")
    public R<List<Map<String, Object>>> userRoles() {
        return R.ok(systemCatalogService.listUserRoles());
    }

    /**
     * 绑定用户角色。
     *
     * @param command 用户角色绑定命令
     * @return 绑定结果
     */
    @PostMapping("/user-roles")
    public R<Map<String, Object>> bindUserRole(@RequestBody Map<String, Object> command) {
        return R.ok(systemCatalogService.bindUserRole(command));
    }

    /**
     * 查询角色菜单授权列表。
     *
     * @return 角色菜单授权列表
     */
    @GetMapping("/role-menus")
    public R<List<Map<String, Object>>> roleMenus() {
        return R.ok(systemCatalogService.listRoleMenus());
    }

    /**
     * 绑定角色菜单。
     *
     * @param command 角色菜单绑定命令
     * @return 绑定结果
     */
    @PostMapping("/role-menus")
    public R<Map<String, Object>> bindRoleMenu(@RequestBody Map<String, Object> command) {
        return R.ok(systemCatalogService.bindRoleMenu(command));
    }
}

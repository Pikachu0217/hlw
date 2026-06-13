package com.hlw.system.controller;

import com.hlw.common.core.domain.R;
import com.hlw.system.dto.BindRoleMenuRequest;
import com.hlw.system.dto.BindUserRoleRequest;
import com.hlw.system.dto.CreateDictRequest;
import com.hlw.system.dto.CreateMenuRequest;
import com.hlw.system.dto.CreatePermissionRequest;
import com.hlw.system.dto.CreatePostRequest;
import com.hlw.system.dto.CreateRoleRequest;
import com.hlw.system.dto.CreateTenantRequest;
import com.hlw.system.dto.CreateUserRequest;
import com.hlw.system.dto.UpdateConfigRequest;
import com.hlw.system.service.SystemTenantContextService;
import com.hlw.system.vo.ConfigVO;
import com.hlw.system.vo.DictVO;
import com.hlw.system.vo.MenuVO;
import com.hlw.system.vo.PermissionVO;
import com.hlw.system.vo.PostVO;
import com.hlw.system.vo.RelationBindingVO;
import com.hlw.system.vo.RoleMenuVO;
import com.hlw.system.vo.RoleVO;
import com.hlw.system.vo.TenantVO;
import com.hlw.system.vo.UserRoleVO;
import com.hlw.system.vo.UserVO;
import jakarta.validation.Valid;
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

/**
 * 系统管理控制器。
 */
@RestController
@RequestMapping("/system")
public class SystemManagementController {
    private static final Logger log = LoggerFactory.getLogger(SystemManagementController.class);

    private final SystemTenantContextService systemTenantContextService;

    /**
     * 构造系统管理控制器。
     *
     * @param systemTenantContextService 系统管理服务
     */
    public SystemManagementController(SystemTenantContextService systemTenantContextService) {
        this.systemTenantContextService = systemTenantContextService;
    }

    /**
     * 查询租户列表。
     *
     * @return 租户列表
     */
    @GetMapping("/tenants")
    public R<List<TenantVO>> tenants() {
        log.info("查询租户列表");
        return R.ok(systemTenantContextService.listTenants());
    }

    /**
     * 创建租户。
     *
     * @param request 创建租户请求
     * @return 创建结果
     */
    @PostMapping("/tenants")
    public R<TenantVO> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        return R.ok(systemTenantContextService.createTenant(request));
    }

    /**
     * 查询后台用户列表。
     *
     * @return 后台用户列表
     */
    @GetMapping("/users")
    public R<List<UserVO>> users() {
        return R.ok(systemTenantContextService.listUsers());
    }

    /**
     * 创建后台用户。
     *
     * @param request 用户创建命令
     * @return 创建后的用户
     */
    @PostMapping("/users")
    public R<UserVO> createUser(@Valid @RequestBody CreateUserRequest request) {
        return R.ok(systemTenantContextService.createUser(request));
    }

    /**
     * 查询角色列表。
     *
     * @return 角色列表
     */
    @GetMapping("/roles")
    public R<List<RoleVO>> roles() {
        return R.ok(systemTenantContextService.listRoles());
    }

    /**
     * 创建角色。
     *
     * @param request 角色创建命令
     * @return 创建后的角色
     */
    @PostMapping("/roles")
    public R<RoleVO> createRole(@Valid @RequestBody CreateRoleRequest request) {
        return R.ok(systemTenantContextService.createRole(request));
    }

    /**
     * 查询菜单列表。
     *
     * @return 菜单列表
     */
    @GetMapping("/menus")
    public R<List<MenuVO>> menus() {
        return R.ok(systemTenantContextService.listMenus());
    }

    /**
     * 创建菜单。
     *
     * @param request 菜单创建命令
     * @return 创建后的菜单
     */
    @PostMapping("/menus")
    public R<MenuVO> createMenu(@Valid @RequestBody CreateMenuRequest request) {
        return R.ok(systemTenantContextService.createMenu(request));
    }

    /**
     * 查询字典列表。
     *
     * @return 字典列表
     */
    @GetMapping("/dicts")
    public R<List<DictVO>> dicts() {
        return R.ok(systemTenantContextService.listDicts());
    }

    /**
     * 创建字典项。
     *
     * @param request 字典创建命令
     * @return 创建后的字典项
     */
    @PostMapping("/dicts")
    public R<DictVO> createDict(@Valid @RequestBody CreateDictRequest request) {
        return R.ok(systemTenantContextService.createDict(request));
    }

    /**
     * 查询系统参数配置列表。
     *
     * @return 系统参数配置列表
     */
    @GetMapping("/configs")
    public R<List<ConfigVO>> configs() {
        return R.ok(systemTenantContextService.listConfigs());
    }

    /**
     * 更新系统参数配置。
     *
     * @param id 配置编号
     * @param request 配置更新命令
     * @return 更新后的配置
     */
    @PutMapping("/configs/{id}")
    public R<ConfigVO> updateConfig(@PathVariable Long id, @Valid @RequestBody UpdateConfigRequest request) {
        return R.ok(systemTenantContextService.updateConfig(id, request));
    }

    /**
     * 查询岗位列表。
     *
     * @return 岗位列表
     */
    @GetMapping("/posts")
    public R<List<PostVO>> posts() {
        return R.ok(systemTenantContextService.listPosts());
    }

    /**
     * 创建岗位。
     *
     * @param request 岗位创建命令
     * @return 创建后的岗位
     */
    @PostMapping("/posts")
    public R<PostVO> createPost(@Valid @RequestBody CreatePostRequest request) {
        return R.ok(systemTenantContextService.createPost(request));
    }

    /**
     * 查询权限码列表。
     *
     * @return 权限码列表
     */
    @GetMapping("/permissions")
    public R<List<PermissionVO>> permissions() {
        return R.ok(systemTenantContextService.listPermissions());
    }

    /**
     * 创建权限码。
     *
     * @param request 权限创建命令
     * @return 创建后的权限码
     */
    @PostMapping("/permissions")
    public R<PermissionVO> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        return R.ok(systemTenantContextService.createPermission(request));
    }

    /**
     * 查询用户角色授权列表。
     *
     * @return 用户角色授权列表
     */
    @GetMapping("/user-roles")
    public R<List<UserRoleVO>> userRoles() {
        return R.ok(systemTenantContextService.listUserRoles());
    }

    /**
     * 绑定用户角色。
     *
     * @param request 用户角色绑定命令
     * @return 绑定结果
     */
    @PostMapping("/user-roles")
    public R<RelationBindingVO> bindUserRole(@Valid @RequestBody BindUserRoleRequest request) {
        return R.ok(systemTenantContextService.bindUserRole(request));
    }

    /**
     * 查询角色菜单授权列表。
     *
     * @return 角色菜单授权列表
     */
    @GetMapping("/role-menus")
    public R<List<RoleMenuVO>> roleMenus() {
        return R.ok(systemTenantContextService.listRoleMenus());
    }

    /**
     * 绑定角色菜单。
     *
     * @param request 角色菜单绑定命令
     * @return 绑定结果
     */
    @PostMapping("/role-menus")
    public R<RelationBindingVO> bindRoleMenu(@Valid @RequestBody BindRoleMenuRequest request) {
        return R.ok(systemTenantContextService.bindRoleMenu(request));
    }
}

package com.hlw.system.controller;

import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
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
import com.hlw.system.dto.UpdateTenantRequest;
import com.hlw.system.service.AuthorizationService;
import com.hlw.system.service.ConfigService;
import com.hlw.system.service.DictService;
import com.hlw.system.service.MenuService;
import com.hlw.system.service.PermissionService;
import com.hlw.system.service.PostService;
import com.hlw.system.service.RoleService;
import com.hlw.system.service.TenantService;
import com.hlw.system.service.UserService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 系统管理控制器。
 */
@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
@Slf4j
public class SystemManagementController {
    /** 租户聚合服务。 */
    private final TenantService tenantService;
    /** 后台用户聚合服务。 */
    private final UserService userService;
    /** 角色聚合服务。 */
    private final RoleService roleService;
    /** 菜单聚合服务。 */
    private final MenuService menuService;
    /** 字典聚合服务。 */
    private final DictService dictService;
    /** 参数配置聚合服务。 */
    private final ConfigService configService;
    /** 岗位聚合服务。 */
    private final PostService postService;
    /** 权限码聚合服务。 */
    private final PermissionService permissionService;
    /** 授权聚合服务，负责用户角色与角色菜单两类绑定。 */
    private final AuthorizationService authorizationService;

    /**
     * 分页查询租户列表。
     *
     * @param query 分页查询参数
     * @return 租户分页结果
     */
    @GetMapping("/tenants")
    public R<PageResult<TenantVO>> tenants(PageQuery query) {
        log.info("查询租户列表");
        return R.ok(tenantService.listTenants(query));
    }

    /**
     * 创建租户。
     *
     * @param request 创建租户请求
     * @return 创建结果
     */
    @PostMapping("/tenants")
    public R<TenantVO> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        return R.ok(tenantService.createTenant(request));
    }

    /**
     * 查询租户详情。
     *
     * @param id 租户编号
     * @return 租户详情
     */
    @GetMapping("/tenants/{id}")
    public R<TenantVO> tenantDetail(@PathVariable Long id) {
        log.info("查询租户详情，id={}", id);
        return R.ok(tenantService.getTenant(id));
    }

    /**
     * 更新租户信息。
     *
     * @param id 租户编号
     * @param request 更新租户请求
     * @return 更新后的租户
     */
    @PutMapping("/tenants/{id}")
    public R<TenantVO> updateTenant(@PathVariable Long id, @Valid @RequestBody UpdateTenantRequest request) {
        log.info("更新租户，id={}", id);
        return R.ok(tenantService.updateTenant(id, request));
    }

    /**
     * 删除租户。
     *
     * @param id 租户编号
     * @return 删除结果
     */
    @DeleteMapping("/tenants/{id}")
    public R<Void> deleteTenant(@PathVariable Long id) {
        log.info("删除租户，id={}", id);
        tenantService.deleteTenant(id);
        return R.ok(null);
    }

    /**
     * 分页查询后台用户列表。
     *
     * @param query 分页查询参数
     * @return 后台用户分页结果
     */
    @GetMapping("/users")
    public R<PageResult<UserVO>> users(PageQuery query) {
        return R.ok(userService.listUsers(query));
    }

    /**
     * 创建后台用户。
     *
     * @param request 用户创建命令
     * @return 创建后的用户
     */
    @PostMapping("/users")
    public R<UserVO> createUser(@Valid @RequestBody CreateUserRequest request) {
        return R.ok(userService.createUser(request));
    }

    /**
     * 分页查询角色列表。
     *
     * @param query 分页查询参数
     * @return 角色分页结果
     */
    @GetMapping("/roles")
    public R<PageResult<RoleVO>> roles(PageQuery query) {
        return R.ok(roleService.listRoles(query));
    }

    /**
     * 创建角色。
     *
     * @param request 角色创建命令
     * @return 创建后的角色
     */
    @PostMapping("/roles")
    public R<RoleVO> createRole(@Valid @RequestBody CreateRoleRequest request) {
        return R.ok(roleService.createRole(request));
    }

    /**
     * 分页查询菜单列表。
     *
     * @param query 分页查询参数
     * @return 菜单分页结果
     */
    @GetMapping("/menus")
    public R<PageResult<MenuVO>> menus(PageQuery query) {
        return R.ok(menuService.listMenus(query));
    }

    /**
     * 创建菜单。
     *
     * @param request 菜单创建命令
     * @return 创建后的菜单
     */
    @PostMapping("/menus")
    public R<MenuVO> createMenu(@Valid @RequestBody CreateMenuRequest request) {
        return R.ok(menuService.createMenu(request));
    }

    /**
     * 分页查询字典列表。
     *
     * @param query 分页查询参数
     * @return 字典分页结果
     */
    @GetMapping("/dicts")
    public R<PageResult<DictVO>> dicts(PageQuery query) {
        return R.ok(dictService.listDicts(query));
    }

    /**
     * 创建字典项。
     *
     * @param request 字典创建命令
     * @return 创建后的字典项
     */
    @PostMapping("/dicts")
    public R<DictVO> createDict(@Valid @RequestBody CreateDictRequest request) {
        return R.ok(dictService.createDict(request));
    }

    /**
     * 分页查询系统参数配置列表。
     *
     * @param query 分页查询参数
     * @return 系统参数配置分页结果
     */
    @GetMapping("/configs")
    public R<PageResult<ConfigVO>> configs(PageQuery query) {
        return R.ok(configService.listConfigs(query));
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
        return R.ok(configService.updateConfig(id, request));
    }

    /**
     * 分页查询岗位列表。
     *
     * @param query 分页查询参数
     * @return 岗位分页结果
     */
    @GetMapping("/posts")
    public R<PageResult<PostVO>> posts(PageQuery query) {
        return R.ok(postService.listPosts(query));
    }

    /**
     * 创建岗位。
     *
     * @param request 岗位创建命令
     * @return 创建后的岗位
     */
    @PostMapping("/posts")
    public R<PostVO> createPost(@Valid @RequestBody CreatePostRequest request) {
        return R.ok(postService.createPost(request));
    }

    /**
     * 分页查询权限码列表。
     *
     * @param query 分页查询参数
     * @return 权限码分页结果
     */
    @GetMapping("/permissions")
    public R<PageResult<PermissionVO>> permissions(PageQuery query) {
        return R.ok(permissionService.listPermissions(query));
    }

    /**
     * 创建权限码。
     *
     * @param request 权限创建命令
     * @return 创建后的权限码
     */
    @PostMapping("/permissions")
    public R<PermissionVO> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        return R.ok(permissionService.createPermission(request));
    }

    /**
     * 查询用户角色授权列表。
     *
     * @return 用户角色授权列表
     */
    @GetMapping("/user-roles")
    public R<List<UserRoleVO>> userRoles() {
        return R.ok(authorizationService.listUserRoles());
    }

    /**
     * 绑定用户角色。
     *
     * @param request 用户角色绑定命令
     * @return 绑定结果
     */
    @PostMapping("/user-roles")
    public R<RelationBindingVO> bindUserRole(@Valid @RequestBody BindUserRoleRequest request) {
        return R.ok(authorizationService.bindUserRole(request));
    }

    /**
     * 查询角色菜单授权列表。
     *
     * @return 角色菜单授权列表
     */
    @GetMapping("/role-menus")
    public R<List<RoleMenuVO>> roleMenus() {
        return R.ok(authorizationService.listRoleMenus());
    }

    /**
     * 绑定角色菜单。
     *
     * @param request 角色菜单绑定命令
     * @return 绑定结果
     */
    @PostMapping("/role-menus")
    public R<RelationBindingVO> bindRoleMenu(@Valid @RequestBody BindRoleMenuRequest request) {
        return R.ok(authorizationService.bindRoleMenu(request));
    }
}

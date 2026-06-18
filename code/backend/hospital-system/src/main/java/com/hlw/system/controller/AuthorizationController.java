package com.hlw.system.controller;

import com.hlw.common.core.domain.R;
import com.hlw.system.domain.req.BindRoleMenuReq;
import com.hlw.system.domain.req.BindUserRoleReq;
import com.hlw.system.service.AuthorizationService;
import com.hlw.system.domain.resp.RelationBindingResp;
import com.hlw.system.domain.resp.RoleMenuResp;
import com.hlw.system.domain.resp.UserRoleResp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 授权关系管理控制器。
 */
@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
@Slf4j
public class AuthorizationController {
    /** 授权聚合服务，负责用户角色与角色菜单两类绑定。 */
    private final AuthorizationService authorizationService;

    /**
     * 查询用户角色授权列表。
     *
     * @return 用户角色授权列表
     */
    @GetMapping("/user-role")
    public R<List<UserRoleResp>> listUserRoles() {
        log.info("查询用户角色授权列表");
        return R.ok(authorizationService.listUserRoles());
    }

    /**
     * 绑定用户角色。
     *
     * @param request 用户角色绑定命令
     * @return 绑定结果
     */
    @PostMapping("/user-role")
    public R<RelationBindingResp> bindUserRole(@Valid @RequestBody BindUserRoleReq request) {
        log.info("绑定用户角色，userId={}，roleId={}", request.getUserId(), request.getRoleId());
        return R.ok(authorizationService.bindUserRole(request));
    }

    /**
     * 查询用户角色授权详情。
     *
     * @param id 授权关系编号
     * @return 用户角色授权详情
     */
    @GetMapping("/user-role/{id}")
    public R<UserRoleResp> getUserRole(@PathVariable Long id) {
        log.info("查询用户角色授权详情，id={}", id);
        return R.ok(authorizationService.getUserRole(id));
    }

    /**
     * 删除用户角色授权。
     *
     * @param id 授权关系编号
     * @return 删除结果
     */
    @DeleteMapping("/user-role/{id}")
    public R<Void> deleteUserRole(@PathVariable Long id) {
        log.info("删除用户角色授权，id={}", id);
        authorizationService.deleteUserRole(id);
        return R.ok(null);
    }

    /**
     * 查询角色菜单授权列表。
     *
     * @return 角色菜单授权列表
     */
    @GetMapping("/role-menu")
    public R<List<RoleMenuResp>> listRoleMenus() {
        log.info("查询角色菜单授权列表");
        return R.ok(authorizationService.listRoleMenus());
    }

    /**
     * 绑定角色菜单。
     *
     * @param request 角色菜单绑定命令
     * @return 绑定结果
     */
    @PostMapping("/role-menu")
    public R<RelationBindingResp> bindRoleMenu(@Valid @RequestBody BindRoleMenuReq request) {
        log.info("绑定角色菜单，roleId={}，menuId={}", request.getRoleId(), request.getMenuId());
        return R.ok(authorizationService.bindRoleMenu(request));
    }

    /**
     * 查询角色菜单授权详情。
     *
     * @param id 授权关系编号
     * @return 角色菜单授权详情
     */
    @GetMapping("/role-menu/{id}")
    public R<RoleMenuResp> getRoleMenu(@PathVariable Long id) {
        log.info("查询角色菜单授权详情，id={}", id);
        return R.ok(authorizationService.getRoleMenu(id));
    }

    /**
     * 删除角色菜单授权。
     *
     * @param id 授权关系编号
     * @return 删除结果
     */
    @DeleteMapping("/role-menu/{id}")
    public R<Void> deleteRoleMenu(@PathVariable Long id) {
        log.info("删除角色菜单授权，id={}", id);
        authorizationService.deleteRoleMenu(id);
        return R.ok(null);
    }
}

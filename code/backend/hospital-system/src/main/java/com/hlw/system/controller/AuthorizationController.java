package com.hlw.system.controller;

import com.hlw.common.core.domain.R;
import com.hlw.system.dto.BindRoleMenuRequest;
import com.hlw.system.dto.BindUserRoleRequest;
import com.hlw.system.service.AuthorizationService;
import com.hlw.system.vo.RelationBindingVO;
import com.hlw.system.vo.RoleMenuVO;
import com.hlw.system.vo.UserRoleVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
public class AuthorizationController {
    /** 授权聚合服务，负责用户角色与角色菜单两类绑定。 */
    private final AuthorizationService authorizationService;

    /**
     * 查询用户角色授权列表。
     *
     * @return 用户角色授权列表
     */
    @GetMapping("/user-role")
    public R<List<UserRoleVO>> listUserRoles() {
        return R.ok(authorizationService.listUserRoles());
    }

    /**
     * 绑定用户角色。
     *
     * @param request 用户角色绑定命令
     * @return 绑定结果
     */
    @PostMapping("/user-role")
    public R<RelationBindingVO> bindUserRole(@Valid @RequestBody BindUserRoleRequest request) {
        return R.ok(authorizationService.bindUserRole(request));
    }

    /**
     * 查询角色菜单授权列表。
     *
     * @return 角色菜单授权列表
     */
    @GetMapping("/role-menu")
    public R<List<RoleMenuVO>> listRoleMenus() {
        return R.ok(authorizationService.listRoleMenus());
    }

    /**
     * 绑定角色菜单。
     *
     * @param request 角色菜单绑定命令
     * @return 绑定结果
     */
    @PostMapping("/role-menu")
    public R<RelationBindingVO> bindRoleMenu(@Valid @RequestBody BindRoleMenuRequest request) {
        return R.ok(authorizationService.bindRoleMenu(request));
    }
}

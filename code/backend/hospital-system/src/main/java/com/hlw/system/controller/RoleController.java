package com.hlw.system.controller;

import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.domain.R;
import com.hlw.system.domain.req.CreateRoleReq;
import com.hlw.system.service.RoleService;
import com.hlw.system.domain.resp.RoleResp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 角色管理控制器。
 */
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
@Slf4j
public class RoleController {
    /** 角色聚合服务。 */
    private final RoleService roleService;

    /**
     * 分页查询角色列表。
     *
     * @param query 分页查询参数
     * @return 角色分页结果
     */
    @GetMapping
    public R<PageResult<RoleResp>> list(PageQuery query) {
        log.info("查询角色列表，keyword={}", query.getKeyword());
        return R.ok(roleService.listRoles(query));
    }

    /**
     * 创建角色。
     *
     * @param request 角色创建命令
     * @return 创建后的角色
     */
    @PostMapping
    public R<RoleResp> createRole(@Valid @RequestBody CreateRoleReq request) {
        log.info("创建角色，roleName={}", request.getRoleName());
        return R.ok(roleService.createRole(request));
    }

    /**
     * 查询角色详情。
     *
     * @param id 角色编号
     * @return 角色详情
     */
    @GetMapping("/{id}")
    public R<RoleResp> detail(@PathVariable Long id) {
        log.info("查询角色详情，id={}", id);
        return R.ok(roleService.getRole(id));
    }

    /**
     * 更新角色。
     *
     * @param id 角色编号
     * @param request 角色更新命令
     * @return 更新后的角色
     */
    @PutMapping("/{id}")
    public R<RoleResp> updateRole(@PathVariable Long id, @Valid @RequestBody CreateRoleReq request) {
        log.info("更新角色，id={}，roleName={}", id, request.getRoleName());
        return R.ok(roleService.updateRole(id, request));
    }

    /**
     * 删除角色。
     *
     * @param id 角色编号
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public R<Void> deleteRole(@PathVariable Long id) {
        log.info("删除角色，id={}", id);
        roleService.deleteRole(id);
        return R.ok(null);
    }
}

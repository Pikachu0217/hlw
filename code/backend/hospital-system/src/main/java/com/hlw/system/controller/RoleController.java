package com.hlw.system.controller;

import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.domain.R;
import com.hlw.system.dto.CreateRoleRequest;
import com.hlw.system.service.RoleService;
import com.hlw.system.vo.RoleVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 角色管理控制器。
 */
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
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
    public R<PageResult<RoleVO>> list(PageQuery query) {
        return R.ok(roleService.listRoles(query));
    }

    /**
     * 创建角色。
     *
     * @param request 角色创建命令
     * @return 创建后的角色
     */
    @PostMapping
    public R<RoleVO> createRole(@Valid @RequestBody CreateRoleRequest request) {
        return R.ok(roleService.createRole(request));
    }
}

package com.hlw.system.controller;

import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.domain.R;
import com.hlw.system.dto.CreatePermissionRequest;
import com.hlw.system.service.PermissionService;
import com.hlw.system.vo.PermissionVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 权限码管理控制器。
 */
@RestController
@RequestMapping("/system/permission")
@RequiredArgsConstructor
public class PermissionController {
    /** 权限码聚合服务。 */
    private final PermissionService permissionService;

    /**
     * 分页查询权限码列表。
     *
     * @param query 分页查询参数
     * @return 权限码分页结果
     */
    @GetMapping
    public R<PageResult<PermissionVO>> list(PageQuery query) {
        return R.ok(permissionService.listPermissions(query));
    }

    /**
     * 创建权限码。
     *
     * @param request 权限创建命令
     * @return 创建后的权限码
     */
    @PostMapping
    public R<PermissionVO> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        return R.ok(permissionService.createPermission(request));
    }
}

package com.hlw.system.controller;

import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.domain.R;
import com.hlw.system.domain.req.CreatePermissionReq;
import com.hlw.system.service.PermissionService;
import com.hlw.system.domain.resp.PermissionResp;
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
 * 权限码管理控制器。
 */
@RestController
@RequestMapping("/system/permission")
@RequiredArgsConstructor
@Slf4j
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
    public R<PageResult<PermissionResp>> list(PageQuery query) {
        log.info("查询权限码列表，keyword={}", query.getKeyword());
        return R.ok(permissionService.listPermissions(query));
    }

    /**
     * 创建权限码。
     *
     * @param request 权限创建命令
     * @return 创建后的权限码
     */
    @PostMapping
    public R<PermissionResp> createPermission(@Valid @RequestBody CreatePermissionReq request) {
        log.info("创建权限码，permissionName={}", request.getPermissionName());
        return R.ok(permissionService.createPermission(request));
    }

    /**
     * 查询权限码详情。
     *
     * @param id 权限码编号
     * @return 权限码详情
     */
    @GetMapping("/{id}")
    public R<PermissionResp> detail(@PathVariable Long id) {
        log.info("查询权限码详情，id={}", id);
        return R.ok(permissionService.getPermission(id));
    }

    /**
     * 更新权限码。
     *
     * @param id 权限码编号
     * @param request 权限更新命令
     * @return 更新后的权限码
     */
    @PutMapping("/{id}")
    public R<PermissionResp> updatePermission(@PathVariable Long id, @Valid @RequestBody CreatePermissionReq request) {
        log.info("更新权限码，id={}，permissionName={}", id, request.getPermissionName());
        return R.ok(permissionService.updatePermission(id, request));
    }

    /**
     * 删除权限码。
     *
     * @param id 权限码编号
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public R<Void> deletePermission(@PathVariable Long id) {
        log.info("删除权限码，id={}", id);
        permissionService.deletePermission(id);
        return R.ok(null);
    }
}

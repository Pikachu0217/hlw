package com.hlw.system.controller;

import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.domain.R;
import com.hlw.system.dto.CreateTenantRequest;
import com.hlw.system.dto.UpdateTenantRequest;
import com.hlw.system.service.TenantService;
import com.hlw.system.vo.TenantVO;
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
 * 租户管理控制器。
 */
@RestController
@RequestMapping("/system/tenant")
@RequiredArgsConstructor
@Slf4j
public class TenantController {
    /** 租户聚合服务。 */
    private final TenantService tenantService;

    /**
     * 分页查询租户列表。
     *
     * @param query 分页查询参数
     * @return 租户分页结果
     */
    @GetMapping
    public R<PageResult<TenantVO>> list(PageQuery query) {
        log.info("查询租户列表");
        return R.ok(tenantService.listTenants(query));
    }

    /**
     * 创建租户。
     *
     * @param request 创建租户请求
     * @return 创建结果
     */
    @PostMapping
    public R<TenantVO> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        return R.ok(tenantService.createTenant(request));
    }

    /**
     * 查询租户详情。
     *
     * @param id 租户编号
     * @return 租户详情
     */
    @GetMapping("/{id}")
    public R<TenantVO> detail(@PathVariable Long id) {
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
    @PutMapping("/{id}")
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
    @DeleteMapping("/{id}")
    public R<Void> deleteTenant(@PathVariable Long id) {
        log.info("删除租户，id={}", id);
        tenantService.deleteTenant(id);
        return R.ok(null);
    }
}

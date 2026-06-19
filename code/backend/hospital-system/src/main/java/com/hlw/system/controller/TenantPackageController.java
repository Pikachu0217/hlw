package com.hlw.system.controller;

import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.domain.R;
import com.hlw.system.domain.req.CreateTenantPackageReq;
import com.hlw.system.domain.resp.TenantPackageResp;
import com.hlw.system.service.TenantPackageService;
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
 * 租户套餐管理控制器。
 */
@RestController
@RequestMapping("/system/tenant-package")
@RequiredArgsConstructor
@Slf4j
public class TenantPackageController {
    /** 租户套餐聚合服务。 */
    private final TenantPackageService tenantPackageService;

    /**
     * 分页查询租户套餐列表。
     *
     * @param query 分页查询参数
     * @return 租户套餐分页结果
     */
    @GetMapping
    public R<PageResult<TenantPackageResp>> list(PageQuery query) {
        log.info("查询租户套餐列表，keyword={}", query.getKeyword());
        return R.ok(tenantPackageService.listPackages(query));
    }

    /**
     * 创建租户套餐。
     *
     * @param request 租户套餐创建命令
     * @return 创建后的租户套餐
     */
    @PostMapping
    public R<TenantPackageResp> createPackage(@Valid @RequestBody CreateTenantPackageReq request) {
        log.info("创建租户套餐，packageName={}", request.getPackageName());
        return R.ok(tenantPackageService.createPackage(request));
    }

    /**
     * 查询租户套餐详情。
     *
     * @param id 租户套餐编号
     * @return 租户套餐详情
     */
    @GetMapping("/{id}")
    public R<TenantPackageResp> detail(@PathVariable Long id) {
        log.info("查询租户套餐详情，id={}", id);
        return R.ok(tenantPackageService.getPackage(id));
    }

    /**
     * 更新租户套餐。
     *
     * @param id 租户套餐编号
     * @param request 租户套餐更新命令
     * @return 更新后的租户套餐
     */
    @PutMapping("/{id}")
    public R<TenantPackageResp> updatePackage(@PathVariable Long id, @Valid @RequestBody CreateTenantPackageReq request) {
        log.info("更新租户套餐，id={}，packageName={}", id, request.getPackageName());
        return R.ok(tenantPackageService.updatePackage(id, request));
    }

    /**
     * 删除租户套餐。
     *
     * @param id 租户套餐编号
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public R<Void> deletePackage(@PathVariable Long id) {
        log.info("删除租户套餐，id={}", id);
        tenantPackageService.deletePackage(id);
        return R.ok(null);
    }
}

package com.hlw.system.controller;

import com.hlw.common.core.domain.R;
import com.hlw.common.core.domain.system.resp.InternalDeptResp;
import com.hlw.system.service.InternalDeptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统内部部门接口，供业务服务通过 Feign 服务间直连调用。
 */
@RestController
@RequestMapping("/internal/depts")
@RequiredArgsConstructor
@Slf4j
public class InternalDeptController {
    /** 内部部门查询服务。 */
    private final InternalDeptService internalDeptService;

    /**
     * 查询租户科室部门列表。
     *
     * @param tenantId 租户编号
     * @return 科室部门列表
     */
    @GetMapping
    public R<List<InternalDeptResp>> listDepartments(@RequestParam Long tenantId) {
        log.info("接收内部科室部门列表查询请求，tenantId={}", tenantId);
        return R.ok(internalDeptService.listDepartments(tenantId));
    }

    /**
     * 查询租户部门详情。
     *
     * @param id 部门编号
     * @param tenantId 租户编号
     * @return 部门详情
     */
    @GetMapping("/{id}")
    public R<InternalDeptResp> detail(@PathVariable Long id, @RequestParam Long tenantId) {
        log.info("接收内部部门详情查询请求，id={}，tenantId={}", id, tenantId);
        return R.ok(internalDeptService.getDept(id, tenantId));
    }
}

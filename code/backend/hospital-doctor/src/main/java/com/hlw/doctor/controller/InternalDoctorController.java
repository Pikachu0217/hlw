package com.hlw.doctor.controller;

import com.hlw.common.core.domain.R;
import com.hlw.doctor.domain.resp.InternalDoctorResp;
import com.hlw.doctor.service.DoctorTenantContextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 医生内部接口控制器。
 */
@RestController
@RequestMapping("/internal/doctors")
@Slf4j
public class InternalDoctorController {
    /** 医生管理服务。 */
    private final DoctorTenantContextService doctorTenantContextService;

    /**
     * 构造医生内部接口控制器。
     *
     * @param doctorTenantContextService 医生管理服务
     */
    public InternalDoctorController(DoctorTenantContextService doctorTenantContextService) {
        this.doctorTenantContextService = doctorTenantContextService;
    }

    /**
     * 按登录用户查询内部医生档案。
     *
     * @param tenantId 租户编号
     * @param userId 登录用户编号
     * @return 内部医生档案
     */
    @GetMapping("/by-user")
    public R<InternalDoctorResp> byUser(@RequestParam Long tenantId, @RequestParam Long userId) {
        log.info("内部查询医生档案，tenantId={}，userId={}", tenantId, userId);
        return R.ok(doctorTenantContextService.getInternalDoctorByUser(tenantId, userId));
    }
}

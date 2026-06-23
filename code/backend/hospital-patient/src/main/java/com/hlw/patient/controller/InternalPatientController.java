package com.hlw.patient.controller;

import com.hlw.common.core.domain.R;
import com.hlw.patient.domain.resp.InternalPatientResp;
import com.hlw.patient.service.PatientTenantContextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 患者内部接口控制器。
 */
@RestController
@RequestMapping("/internal/patients")
@Slf4j
public class InternalPatientController {
    /** 患者管理服务。 */
    private final PatientTenantContextService patientTenantContextService;

    /**
     * 构造患者内部接口控制器。
     *
     * @param patientTenantContextService 患者管理服务
     */
    public InternalPatientController(PatientTenantContextService patientTenantContextService) {
        this.patientTenantContextService = patientTenantContextService;
    }

    /**
     * 按登录用户查询内部患者档案。
     *
     * @param tenantId 租户编号
     * @param userId 登录用户编号
     * @return 内部患者档案
     */
    @GetMapping("/by-user")
    public R<InternalPatientResp> byUser(@RequestParam Long tenantId, @RequestParam Long userId) {
        log.info("内部查询患者档案，tenantId={}，userId={}", tenantId, userId);
        return R.ok(patientTenantContextService.getInternalPatientByUser(tenantId, userId));
    }
}

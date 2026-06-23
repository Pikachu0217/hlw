package com.hlw.patient.controller;

import com.hlw.common.core.domain.R;
import com.hlw.patient.domain.req.CreatePatientInternalReq;
import com.hlw.patient.domain.resp.InternalPatientResp;
import com.hlw.patient.service.PatientTenantContextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
     * @param userId   关联用户编号（sys_user.user_id 字符串）
     * @return 内部患者档案
     */
    @GetMapping("/by-user")
    public R<InternalPatientResp> byUser(@RequestParam Long tenantId, @RequestParam String userId) {
        log.info("内部查询患者档案，tenantId={}，userId={}", tenantId, userId);
        return R.ok(patientTenantContextService.getInternalPatientByUser(tenantId, userId));
    }

    /**
     * 创建或获取患者档案（手机号注册后自动绑定）。
     *
     * @param req 创建请求
     * @return 内部患者档案
     */
    @PostMapping
    public R<InternalPatientResp> createOrGetByUser(@RequestBody CreatePatientInternalReq req) {
        log.info("内部创建或获取患者档案，tenantId={}，userId={}，phone={}", req.getTenantId(), req.getUserId(), req.getPhone());
        return R.ok(patientTenantContextService.createOrGetPatientByUser(req.getTenantId(), req.getUserId(), req.getPhone()));
    }
}

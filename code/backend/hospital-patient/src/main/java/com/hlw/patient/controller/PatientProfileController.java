package com.hlw.patient.controller;

import com.hlw.common.core.domain.R;
import com.hlw.patient.dto.CreateHealthRecordRequest;
import com.hlw.patient.dto.CreatePatientRequest;
import com.hlw.patient.dto.UpdatePatientProfileRequest;
import com.hlw.patient.service.PatientTenantContextService;
import com.hlw.patient.vo.HealthRecordVO;
import com.hlw.patient.vo.PatientProfileVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 患者档案控制器。
 */
@RestController
@RequestMapping("/patient")
@Slf4j
public class PatientProfileController {
    private final PatientTenantContextService patientTenantContextService;

    /**
     * 构造患者档案控制器。
     *
     * @param patientTenantContextService 患者管理服务
     */
    public PatientProfileController(PatientTenantContextService patientTenantContextService) {
        this.patientTenantContextService = patientTenantContextService;
    }

    /**
     * 查询当前患者档案。
     *
     * @return 患者档案
     */
    @GetMapping("/profile")
    public R<PatientProfileVO> profile() {
        log.info("查询当前患者档案");
        return R.ok(patientTenantContextService.getCurrentProfile());
    }

    /**
     * 查询患者列表。
     *
     * @return 患者列表
     */
    @GetMapping("/patients")
    public R<List<PatientProfileVO>> patients() {
        return R.ok(patientTenantContextService.listPatients());
    }

    /**
     * 查询患者详情。
     *
     * @param id 患者编号
     * @return 患者详情
     */
    @GetMapping("/patients/{id}")
    public R<PatientProfileVO> patientDetail(@PathVariable Long id) {
        return R.ok(patientTenantContextService.getPatient(id));
    }

    /**
     * 创建患者档案。
     *
     * @param request 创建患者请求
     * @return 患者详情
     */
    @PostMapping("/patients")
    public R<PatientProfileVO> createPatient(@Valid @RequestBody CreatePatientRequest request) {
        return R.ok(patientTenantContextService.createPatient(request));
    }

    /**
     * 更新当前患者档案。
     *
     * @param request 更新命令
     * @return 患者档案
     */
    @PutMapping("/profile")
    public R<PatientProfileVO> updateProfile(@Valid @RequestBody UpdatePatientProfileRequest request) {
        return R.ok(patientTenantContextService.updateCurrentProfile(request));
    }

    /**
     * 更新患者档案。
     *
     * @param id 患者编号
     * @param request 更新患者资料请求
     * @return 患者详情
     */
    @PutMapping("/patients/{id}")
    public R<PatientProfileVO> updatePatient(@PathVariable Long id, @Valid @RequestBody UpdatePatientProfileRequest request) {
        return R.ok(patientTenantContextService.updatePatient(id, request));
    }

    /**
     * 查询健康档案列表。
     *
     * @param patientId 患者编号
     * @return 健康档案列表
     */
    @GetMapping("/health-records")
    public R<List<HealthRecordVO>> healthRecords(@RequestParam(required = false) Long patientId) {
        return R.ok(patientTenantContextService.listHealthRecords(patientId));
    }

    /**
     * 创建健康档案。
     *
     * @param request 创建命令
     * @return 创建结果
     */
    @PostMapping("/health-records")
    public R<HealthRecordVO> createHealthRecord(@Valid @RequestBody CreateHealthRecordRequest request) {
        return R.ok(patientTenantContextService.createHealthRecord(request));
    }
}

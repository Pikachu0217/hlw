package com.hlw.patient.controller;

import com.hlw.common.core.domain.R;
import com.hlw.patient.service.PatientProfile;
import com.hlw.patient.service.PatientProfileService;
import com.hlw.patient.service.UpdatePatientProfileCommand;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/patient")
public class PatientProfileController {
    private final PatientProfileService patientProfileService;

    /**
     * 构造患者档案控制器。
     *
     * @param patientProfileService 患者档案服务
     */
    public PatientProfileController(PatientProfileService patientProfileService) {
        this.patientProfileService = patientProfileService;
    }

    /**
     * 查询当前患者档案。
     *
     * @return 患者档案
     */
    @GetMapping("/profile")
    public R<PatientProfile> profile() {
        return R.ok(patientProfileService.findProfile(1L));
    }

    /**
     * 更新当前患者档案。
     *
     * @param command 更新命令
     * @return 患者档案
     */
    @PutMapping("/profile")
    public R<PatientProfile> updateProfile(@RequestBody UpdatePatientProfileCommand command) {
        return R.ok(patientProfileService.updateProfile(1L, command));
    }

    /**
     * 查询健康档案演示列表。
     *
     * @return 健康档案列表
     */
    @GetMapping("/health-records")
    public R<List<Map<String, Object>>> healthRecords() {
        return R.ok(List.of(
            Map.of("id", 1L, "title", "发热问诊", "summary", "儿童发热 12 小时，已线上问诊"),
            Map.of("id", 2L, "title", "复诊续方", "summary", "慢病用药复诊记录")
        ));
    }

    /**
     * 创建健康档案。
     *
     * @param command 创建命令
     * @return 创建结果
     */
    @PostMapping("/health-records")
    public R<Map<String, Object>> createHealthRecord(@RequestBody Map<String, Object> command) {
        return R.ok(command);
    }
}

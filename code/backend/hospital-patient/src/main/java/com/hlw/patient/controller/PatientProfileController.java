package com.hlw.patient.controller;

import com.hlw.common.core.domain.R;
import com.hlw.common.core.jdbc.DemoDataQuery;
import com.hlw.patient.service.PatientProfile;
import com.hlw.patient.service.PatientProfileService;
import com.hlw.patient.service.UpdatePatientProfileCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(PatientProfileController.class);

    private final PatientProfileService patientProfileService;
    private final DemoDataQuery demoDataQuery;

    /**
     * 构造患者档案控制器。
     *
     * @param patientProfileService 患者档案服务
     * @param demoDataQuery 演示数据查询器
     */
    public PatientProfileController(PatientProfileService patientProfileService, DemoDataQuery demoDataQuery) {
        this.patientProfileService = patientProfileService;
        this.demoDataQuery = demoDataQuery;
    }

    /**
     * 查询当前患者档案。
     *
     * @return 患者档案
     */
    @GetMapping("/profile")
    public R<PatientProfile> profile() {
        log.info("查询当前患者档案");
        Map<String, Object> profile = demoDataQuery.one("当前患者档案", """
            SELECT id,
                   patient_name AS name,
                   phone,
                   gender
            FROM pat_patient
            WHERE deleted = 0
            ORDER BY id
            LIMIT 1
            """);
        if (profile.isEmpty()) {
            throw new IllegalStateException("当前患者档案示例数据未初始化");
        }
        return R.ok(new PatientProfile(
            ((Number) profile.get("id")).longValue(),
            String.valueOf(profile.get("name")),
            patientProfileService.maskPhone(String.valueOf(profile.get("phone"))),
            String.valueOf(profile.get("gender"))
        ));
    }

    /**
     * 查询患者列表。
     *
     * @return 患者列表
     */
    @GetMapping("/patients")
    public R<List<Map<String, Object>>> patients() {
        log.info("查询患者列表");
        return R.ok(demoDataQuery.list("患者列表", """
            SELECT id::text AS key,
                   patient_name AS "patientName",
                   gender AS gender,
                   age AS age,
                   risk_level AS "riskLevel",
                   phone AS phone,
                   to_char(last_visit, 'YYYY-MM-DD') AS "lastVisit"
            FROM pat_patient
            WHERE deleted = 0
            ORDER BY id
            """));
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
        log.info("查询健康档案列表");
        return R.ok(demoDataQuery.list("健康档案列表", """
            SELECT id AS id,
                   title AS title,
                   summary AS summary
            FROM pat_health_record
            WHERE deleted = 0
            ORDER BY id
            """));
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

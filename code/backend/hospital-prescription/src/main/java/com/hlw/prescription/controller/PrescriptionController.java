package com.hlw.prescription.controller;

import com.hlw.common.core.domain.R;
import com.hlw.common.core.jdbc.DemoDataQuery;
import com.hlw.prescription.service.PrescriptionWorkflowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.List;

/**
 * 处方管理控制器，提供处方创建、提审、审核与驳回接口。
 */
@RestController
@RequestMapping("/prescription")
public class PrescriptionController {
    private static final Logger log = LoggerFactory.getLogger(PrescriptionController.class);

    private final PrescriptionWorkflowService prescriptionWorkflowService;
    private final DemoDataQuery demoDataQuery;

    /**
     * 构造处方控制器。
     *
     * @param prescriptionWorkflowService 处方工作流服务
     * @param demoDataQuery 演示数据查询器
     */
    public PrescriptionController(PrescriptionWorkflowService prescriptionWorkflowService, DemoDataQuery demoDataQuery) {
        this.prescriptionWorkflowService = prescriptionWorkflowService;
        this.demoDataQuery = demoDataQuery;
    }

    /**
     * 查询处方列表。
     *
     * @return 处方列表
     */
    @GetMapping("/prescriptions")
    public R<List<Map<String, Object>>> prescriptions() {
        log.info("查询处方列表");
        return R.ok(demoDataQuery.list("处方列表", """
            SELECT id::text AS key,
                   prescription_no AS "prescriptionNo",
                   patient_name AS "patientName",
                   doctor_name AS "doctorName",
                   drug_count AS "drugCount",
                   issued_at AS "issuedAt",
                   status AS status
            FROM pre_prescription
            WHERE deleted = 0
            ORDER BY id
            """));
    }

    /**
     * 创建处方草稿。
     *
     * @param command 创建命令
     * @return 创建结果
     */
    @PostMapping("/prescriptions")
    public R<Map<String, Object>> create(@RequestBody Map<String, Object> command) {
        return R.ok(prescriptionWorkflowService.create(command));
    }

    /**
     * 提交处方进入待审核状态。
     *
     * @param id 处方编号
     * @return 提交结果
     */
    @PostMapping("/prescriptions/{id}/submit")
    public R<Map<String, Object>> submit(@PathVariable Long id) {
        return R.ok(prescriptionWorkflowService.submit(id));
    }

    /**
     * 审核通过处方。
     *
     * @param id 处方编号
     * @param command 审核命令
     * @return 审核后的处方
     */
    @PostMapping("/prescriptions/{id}/approve")
    public R<Map<String, Object>> approve(@PathVariable Long id, @RequestBody Map<String, Object> command) {
        Long pharmacistId = Long.valueOf(String.valueOf(command.getOrDefault("pharmacistId", 0L)));
        String remark = String.valueOf(command.getOrDefault("remark", ""));
        return R.ok(prescriptionWorkflowService.approve(id, pharmacistId, remark));
    }

    /**
     * 驳回处方并返回驳回结果。
     *
     * @param id 处方编号
     * @param command 驳回命令
     * @return 驳回结果
     */
    @PostMapping("/prescriptions/{id}/reject")
    public R<Map<String, Object>> reject(@PathVariable Long id, @RequestBody Map<String, Object> command) {
        String remark = String.valueOf(command.getOrDefault("remark", ""));
        return R.ok(prescriptionWorkflowService.reject(id, remark));
    }
}

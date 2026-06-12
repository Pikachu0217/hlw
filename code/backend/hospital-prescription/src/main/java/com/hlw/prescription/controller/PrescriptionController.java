package com.hlw.prescription.controller;

import com.hlw.common.core.domain.R;
import com.hlw.prescription.service.Prescription;
import com.hlw.prescription.service.PrescriptionAuditService;
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

    private final PrescriptionAuditService prescriptionAuditService;

    /**
     * 构造处方控制器。
     *
     * @param prescriptionAuditService 处方审核服务
     */
    public PrescriptionController(PrescriptionAuditService prescriptionAuditService) {
        this.prescriptionAuditService = prescriptionAuditService;
    }

    /**
     * 查询处方列表。
     *
     * @return 处方列表
     */
    @GetMapping("/prescriptions")
    public R<List<Map<String, Object>>> prescriptions() {
        log.info("查询处方列表");
        return R.ok(List.of(
            Map.of("key", "1", "prescriptionNo", "CF20260612001", "patientName", "赵晓岚", "doctorName", "陈知衡", "drugCount", 3, "issuedAt", "09:42", "status", "待审方"),
            Map.of("key", "2", "prescriptionNo", "CF20260612002", "patientName", "沈博远", "doctorName", "顾清和", "drugCount", 5, "issuedAt", "09:18", "status", "待发药")
        ));
    }

    /**
     * 创建处方草稿。
     *
     * @param command 创建命令
     * @return 创建结果
     */
    @PostMapping("/prescriptions")
    public R<Map<String, Object>> create(@RequestBody Map<String, Object> command) {
        return R.ok(command);
    }

    /**
     * 提交处方进入待审核状态。
     *
     * @param id 处方编号
     * @return 提交结果
     */
    @PostMapping("/prescriptions/{id}/submit")
    public R<Map<String, Object>> submit(@PathVariable Long id) {
        return R.ok(Map.of("id", id, "status", "SUBMITTED"));
    }

    /**
     * 审核通过处方。
     *
     * @param id 处方编号
     * @param command 审核命令
     * @return 审核后的处方
     */
    @PostMapping("/prescriptions/{id}/approve")
    public R<Prescription> approve(@PathVariable Long id, @RequestBody Map<String, Object> command) {
        Long pharmacistId = Long.valueOf(String.valueOf(command.getOrDefault("pharmacistId", 0L)));
        String remark = String.valueOf(command.getOrDefault("remark", ""));
        return R.ok(prescriptionAuditService.approve(id, pharmacistId, remark));
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
        return R.ok(Map.of("id", id, "status", "REJECTED", "remark", command.get("remark")));
    }
}

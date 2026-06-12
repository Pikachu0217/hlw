package com.hlw.prescription.controller;

import com.hlw.common.core.domain.R;
import com.hlw.prescription.service.Prescription;
import com.hlw.prescription.service.PrescriptionAuditService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 处方管理控制器，提供处方创建、提审、审核与驳回接口。
 */
@RestController
@RequestMapping("/prescription")
public class PrescriptionController {
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

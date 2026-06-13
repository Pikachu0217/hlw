package com.hlw.prescription.controller;

import com.hlw.common.core.domain.R;
import com.hlw.prescription.dto.ApprovePrescriptionRequest;
import com.hlw.prescription.dto.CreatePrescriptionRequest;
import com.hlw.prescription.dto.RejectPrescriptionRequest;
import com.hlw.prescription.service.PrescriptionWorkflowService;
import com.hlw.prescription.vo.PrescriptionVO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 处方管理控制器，提供处方创建、提审、审核与驳回接口。
 */
@RestController
@RequestMapping("/prescription")
public class PrescriptionController {
    private static final Logger log = LoggerFactory.getLogger(PrescriptionController.class);

    private final PrescriptionWorkflowService prescriptionWorkflowService;

    /**
     * 构造处方控制器。
     *
     * @param prescriptionWorkflowService 处方工作流服务
     */
    public PrescriptionController(PrescriptionWorkflowService prescriptionWorkflowService) {
        this.prescriptionWorkflowService = prescriptionWorkflowService;
    }

    /**
     * 查询处方列表。
     *
     * @return 处方列表
     */
    @GetMapping("/prescriptions")
    public R<List<PrescriptionVO>> prescriptions() {
        log.info("查询处方列表");
        return R.ok(prescriptionWorkflowService.listPrescriptions());
    }

    /**
     * 创建处方草稿。
     *
     * @param request 创建请求
     * @return 创建结果
     */
    @PostMapping("/prescriptions")
    public R<PrescriptionVO> create(@Valid @RequestBody CreatePrescriptionRequest request) {
        return R.ok(prescriptionWorkflowService.create(request));
    }

    /**
     * 提交处方进入待审核状态。
     *
     * @param id 处方编号
     * @return 提交结果
     */
    @PostMapping("/prescriptions/{id}/submit")
    public R<PrescriptionVO> submit(@PathVariable Long id) {
        return R.ok(prescriptionWorkflowService.submit(id));
    }

    /**
     * 审核通过处方。
     *
     * @param id 处方编号
     * @param request 审核请求
     * @return 审核后的处方
     */
    @PostMapping("/prescriptions/{id}/approve")
    public R<PrescriptionVO> approve(@PathVariable Long id, @RequestBody ApprovePrescriptionRequest request) {
        return R.ok(prescriptionWorkflowService.approve(id, request));
    }

    /**
     * 驳回处方并返回驳回结果。
     *
     * @param id 处方编号
     * @param request 驳回请求
     * @return 驳回结果
     */
    @PostMapping("/prescriptions/{id}/reject")
    public R<PrescriptionVO> reject(@PathVariable Long id, @RequestBody RejectPrescriptionRequest request) {
        return R.ok(prescriptionWorkflowService.reject(id, request));
    }
}

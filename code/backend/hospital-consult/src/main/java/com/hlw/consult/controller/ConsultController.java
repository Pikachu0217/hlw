package com.hlw.consult.controller;

import com.hlw.common.core.domain.R;
import com.hlw.consult.dto.AcceptConsultRequest;
import com.hlw.consult.dto.CreateConsultRequest;
import com.hlw.consult.service.ConsultWorkflowService;
import com.hlw.consult.service.DoctorConsultWorkbenchService;
import com.hlw.consult.vo.ConsultVO;
import com.hlw.consult.vo.DoctorConsultWorkbenchVO;
import com.hlw.consult.ws.ConsultMessage;
import com.hlw.consult.ws.ConsultMessageRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 问诊管理控制器。
 */
@RestController
@RequestMapping("/consult")
@Slf4j
public class ConsultController {
    private final ConsultWorkflowService consultWorkflowService;
    private final ConsultMessageRepository consultMessageRepository;
    private final DoctorConsultWorkbenchService doctorConsultWorkbenchService;

    /**
     * 构造问诊控制器。
     *
     * @param consultWorkflowService 问诊工作流服务
     * @param consultMessageRepository 问诊消息仓储
     * @param doctorConsultWorkbenchService 医生咨询工作台服务
     */
    public ConsultController(
        ConsultWorkflowService consultWorkflowService,
        ConsultMessageRepository consultMessageRepository,
        DoctorConsultWorkbenchService doctorConsultWorkbenchService
    ) {
        this.consultWorkflowService = consultWorkflowService;
        this.consultMessageRepository = consultMessageRepository;
        this.doctorConsultWorkbenchService = doctorConsultWorkbenchService;
    }

    /**
     * 查询问诊单列表。
     *
     * @return 问诊单列表
     */
    @GetMapping("/consults")
    public R<List<ConsultVO>> consults() {
        log.info("查询问诊单列表");
        return R.ok(consultWorkflowService.listConsults());
    }

    /**
     * 创建问诊。
     *
     * @param request 创建请求
     * @return 创建结果
     */
    @PostMapping("/consults")
    public R<ConsultVO> createConsult(@Valid @RequestBody CreateConsultRequest request) {
        return R.ok(consultWorkflowService.createConsult(request));
    }

    /**
     * 接单问诊。
     *
     * @param id 问诊编号
     * @param request 接单请求
     * @return 接单结果
     */
    @PostMapping("/consults/{id}/accept")
    public R<ConsultVO> accept(@PathVariable Long id, @RequestBody AcceptConsultRequest request) {
        return R.ok(consultWorkflowService.accept(id, request));
    }

    /**
     * 完成问诊。
     *
     * @param id 问诊编号
     * @return 完成结果
     */
    @PostMapping("/consults/{id}/complete")
    public R<ConsultVO> complete(@PathVariable Long id) {
        return R.ok(consultWorkflowService.complete(id));
    }

    /**
     * 延长问诊。
     *
     * @param id 问诊编号
     * @return 延长结果
     */
    @PostMapping("/consults/{id}/extend")
    public R<ConsultVO> extend(@PathVariable Long id) {
        return R.ok(consultWorkflowService.extend(id));
    }

    /**
     * 查询当前登录医生咨询工作台。
     *
     * @return 医生咨询工作台列表
     */
    @GetMapping("/doctor/workbench")
    public R<List<DoctorConsultWorkbenchVO>> doctorWorkbench() {
        log.info("查询当前登录医生咨询工作台");
        return R.ok(doctorConsultWorkbenchService.listCurrentDoctorWorkbench());
    }

    /**
     * 从已支付预约单创建问诊。
     *
     * @param appointmentId 预约单编号
     * @return 问诊单
     */
    @PostMapping("/consults/from-appointment/{appointmentId}")
    public R<ConsultVO> createConsultFromAppointment(@PathVariable Long appointmentId) {
        log.info("从预约单创建问诊，appointmentId={}", appointmentId);
        return R.ok(consultWorkflowService.createConsultFromAppointment(appointmentId));
    }

    /**
     * 查询问诊消息。
     *
     * @param id 问诊编号
     * @return 消息列表
     */
    @GetMapping("/consults/{id}/messages")
    public R<List<ConsultMessage>> messages(@PathVariable Long id) {
        log.info("查询问诊消息，consultId={}", id);
        return R.ok(consultMessageRepository.findByConsultId(id));
    }
}

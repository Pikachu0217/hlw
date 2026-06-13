package com.hlw.appointment.controller;

import com.hlw.appointment.dto.CreateAppointmentRequest;
import com.hlw.appointment.dto.CreateReleaseConfigRequest;
import com.hlw.appointment.dto.GrabAppointmentRequest;
import com.hlw.appointment.service.AppointmentWorkflowService;
import com.hlw.appointment.vo.AppointmentVO;
import com.hlw.appointment.vo.NumberSourceVO;
import com.hlw.appointment.vo.ReleaseConfigVO;
import com.hlw.common.core.domain.R;
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
 * 预约管理控制器。
 */
@RestController
@RequestMapping("/appointment")
public class AppointmentController {
    private static final Logger log = LoggerFactory.getLogger(AppointmentController.class);

    private final AppointmentWorkflowService appointmentWorkflowService;

    /**
     * 构造预约控制器。
     *
     * @param appointmentWorkflowService 预约工作流服务
     */
    public AppointmentController(AppointmentWorkflowService appointmentWorkflowService) {
        this.appointmentWorkflowService = appointmentWorkflowService;
    }

    /**
     * 查询预约单列表。
     *
     * @return 预约单列表
     */
    @GetMapping("/appointments")
    public R<List<AppointmentVO>> appointments() {
        log.info("查询预约单列表");
        return R.ok(appointmentWorkflowService.listAppointments());
    }

    /**
     * 查询号源列表。
     *
     * @return 号源列表
     */
    @GetMapping("/number-sources")
    public R<List<NumberSourceVO>> numberSources() {
        log.info("查询号源列表");
        return R.ok(appointmentWorkflowService.listNumberSources());
    }

    /**
     * 创建预约单。
     *
     * @param request 创建命令
     * @return 创建结果
     */
    @PostMapping("/appointments")
    public R<AppointmentVO> createAppointment(@Valid @RequestBody CreateAppointmentRequest request) {
        return R.ok(appointmentWorkflowService.createAppointment(request));
    }

    /**
     * 支付预约单。
     *
     * @param id 预约编号
     * @return 支付结果
     */
    @PostMapping("/appointments/{id}/pay")
    public R<AppointmentVO> pay(@PathVariable Long id) {
        return R.ok(appointmentWorkflowService.pay(id));
    }

    /**
     * 预约签到。
     *
     * @param id 预约编号
     * @return 签到结果
     */
    @PostMapping("/appointments/{id}/check-in")
    public R<AppointmentVO> checkIn(@PathVariable Long id) {
        return R.ok(appointmentWorkflowService.checkIn(id));
    }

    /**
     * 抢便民门诊预约单。
     *
     * @param id 预约编号
     * @param command 抢单命令
     * @return 是否抢单成功
     */
    @PostMapping("/appointments/{id}/grab")
    public R<Boolean> grab(@PathVariable Long id, @Valid @RequestBody GrabAppointmentRequest request) {
        return R.ok(appointmentWorkflowService.grab(id, request.getDoctorId()));
    }

    /**
     * 锁定号源。
     *
     * @param scheduleId 排班编号
     * @return 号源
     */
    @PostMapping("/number-sources/{scheduleId}/lock")
    public R<NumberSourceVO> lockNumberSource(@PathVariable Long scheduleId) {
        return R.ok(appointmentWorkflowService.lockNumberSource(scheduleId));
    }

    /**
     * 创建放号配置。
     *
     * @param request 创建命令
     * @return 创建结果
     */
    @PostMapping("/release-configs")
    public R<ReleaseConfigVO> createReleaseConfig(@Valid @RequestBody CreateReleaseConfigRequest request) {
        return R.ok(appointmentWorkflowService.createReleaseConfig(request));
    }
}

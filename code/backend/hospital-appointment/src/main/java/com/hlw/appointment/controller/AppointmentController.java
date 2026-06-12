package com.hlw.appointment.controller;

import com.hlw.appointment.service.Appointment;
import com.hlw.appointment.service.GrabAppointmentService;
import com.hlw.appointment.service.NumberSource;
import com.hlw.appointment.service.NumberSourceStatus;
import com.hlw.appointment.service.NumberSourceService;
import com.hlw.common.core.domain.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/appointment")
public class AppointmentController {
    private static final Logger log = LoggerFactory.getLogger(AppointmentController.class);

    private final NumberSourceService numberSourceService;
    private final GrabAppointmentService grabAppointmentService;

    /**
     * 构造预约控制器。
     *
     * @param numberSourceService 号源服务
     * @param grabAppointmentService 抢单服务
     */
    public AppointmentController(NumberSourceService numberSourceService, GrabAppointmentService grabAppointmentService) {
        this.numberSourceService = numberSourceService;
        this.grabAppointmentService = grabAppointmentService;
    }

    /**
     * 查询预约单列表。
     *
     * @return 预约单列表
     */
    @GetMapping("/appointments")
    public R<List<Map<String, Object>>> appointments() {
        log.info("查询预约单列表");
        return R.ok(List.of(
            Map.of("key", "1", "appointmentNo", "YY20260612001", "patientName", "赵晓岚", "doctorName", "陈知衡", "clinicTime", "今天 14:00", "source", "小程序", "status", "待就诊"),
            Map.of("key", "2", "appointmentNo", "YY20260612002", "patientName", "沈博远", "doctorName", "顾清和", "clinicTime", "今天 15:30", "source", "客服代约", "status", "已签到")
        ));
    }

    /**
     * 查询号源演示列表。
     *
     * @return 号源列表
     */
    @GetMapping("/number-sources")
    public R<List<NumberSource>> numberSources() {
        log.info("查询号源列表");
        return R.ok(List.of(
            new NumberSource(1L, 1L, 1, NumberSourceStatus.AVAILABLE),
            new NumberSource(2L, 1L, 2, NumberSourceStatus.AVAILABLE)
        ));
    }

    /**
     * 创建预约单。
     *
     * @param command 创建命令
     * @return 创建结果
     */
    @PostMapping("/appointments")
    public R<Map<String, Object>> createAppointment(@RequestBody Map<String, Object> command) {
        return R.ok(Map.of(
            "id", 1L,
            "appointmentNo", "YY20260612001",
            "status", "PENDING_PAY",
            "doctorName", command.getOrDefault("doctorName", "陈知衡")
        ));
    }

    /**
     * 支付预约单。
     *
     * @param id 预约编号
     * @return 支付结果
     */
    @PostMapping("/appointments/{id}/pay")
    public R<Map<String, Object>> pay(@PathVariable Long id) {
        return R.ok(Map.of("id", id, "status", "PAID"));
    }

    /**
     * 预约签到。
     *
     * @param id 预约编号
     * @return 签到结果
     */
    @PostMapping("/appointments/{id}/check-in")
    public R<Map<String, Object>> checkIn(@PathVariable Long id) {
        return R.ok(Map.of("id", id, "status", "CHECKED_IN"));
    }

    /**
     * 抢便民门诊预约单。
     *
     * @param id 预约编号
     * @param command 抢单命令
     * @return 是否抢单成功
     */
    @PostMapping("/appointments/{id}/grab")
    public R<Boolean> grab(@PathVariable Long id, @RequestBody Map<String, Long> command) {
        return R.ok(grabAppointmentService.grab(id, command.get("doctorId")));
    }

    /**
     * 锁定号源。
     *
     * @param scheduleId 排班编号
     * @return 号源
     */
    @PostMapping("/number-sources/{scheduleId}/lock")
    public R<NumberSource> lockNumberSource(@PathVariable Long scheduleId) {
        return R.ok(numberSourceService.lockOne(scheduleId));
    }

    /**
     * 创建放号配置。
     *
     * @param command 创建命令
     * @return 创建结果
     */
    @PostMapping("/release-configs")
    public R<Map<String, Object>> createReleaseConfig(@RequestBody Map<String, Object> command) {
        return R.ok(command);
    }
}

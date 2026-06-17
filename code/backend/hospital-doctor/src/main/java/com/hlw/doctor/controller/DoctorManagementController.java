package com.hlw.doctor.controller;

import com.hlw.common.core.domain.R;
import com.hlw.doctor.dto.BindDoctorDepartmentRequest;
import com.hlw.doctor.dto.CreateDepartmentRequest;
import com.hlw.doctor.dto.CreateDoctorRequest;
import com.hlw.doctor.dto.CreateScheduleRequest;
import com.hlw.doctor.dto.ResolveAppointmentFeeRequest;
import com.hlw.doctor.dto.UpdateDoctorStatusRequest;
import com.hlw.doctor.service.AppointmentFeePolicyService;
import com.hlw.doctor.service.DoctorTenantContextService;
import com.hlw.doctor.vo.DepartmentVO;
import com.hlw.doctor.vo.DoctorDepartmentBindingVO;
import com.hlw.doctor.vo.DoctorVO;
import com.hlw.doctor.vo.ScheduleVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 医生管理控制器。
 */
@RestController
@RequestMapping("/doctor")
@Slf4j
public class DoctorManagementController {
    private final DoctorTenantContextService doctorTenantContextService;
    private final AppointmentFeePolicyService appointmentFeePolicyService;

    /**
     * 构造医生管理控制器。
     *
     * @param doctorTenantContextService 医生管理服务
     * @param appointmentFeePolicyService 挂号费策略服务
     */
    public DoctorManagementController(
        DoctorTenantContextService doctorTenantContextService,
        AppointmentFeePolicyService appointmentFeePolicyService
    ) {
        this.doctorTenantContextService = doctorTenantContextService;
        this.appointmentFeePolicyService = appointmentFeePolicyService;
    }

    /**
     * 查询科室列表。
     *
     * @return 科室列表
     */
    @GetMapping("/departments")
    public R<List<DepartmentVO>> departments() {
        return R.ok(doctorTenantContextService.listDepartments());
    }

    /**
     * 创建科室。
     *
     * @param request 创建科室请求
     * @return 科室展示对象
     */
    @PostMapping("/departments")
    public R<DepartmentVO> createDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        return R.ok(doctorTenantContextService.createDepartment(request));
    }

    /**
     * 查询医生列表。
     *
     * @return 医生列表
     */
    @GetMapping("/doctors")
    public R<List<DoctorVO>> doctors() {
        log.info("查询医生列表");
        return R.ok(doctorTenantContextService.listDoctors());
    }

    /**
     * 查询医生详情。
     *
     * @param id 医生编号
     * @return 医生详情
     */
    @GetMapping("/doctors/{id}")
    public R<DoctorVO> doctorDetail(@PathVariable Long id) {
        return R.ok(doctorTenantContextService.getDoctor(id));
    }

    /**
     * 创建医生。
     *
     * @param request 创建医生请求
     * @return 医生展示对象
     */
    @PostMapping("/doctors")
    public R<DoctorVO> createDoctor(@Valid @RequestBody CreateDoctorRequest request) {
        return R.ok(doctorTenantContextService.createDoctor(request));
    }

    /**
     * 更新医生状态。
     *
     * @param id 医生编号
     * @param request 状态请求
     * @return 医生展示对象
     */
    @PutMapping("/doctors/{id}/status")
    public R<DoctorVO> updateDoctorStatus(@PathVariable Long id, @Valid @RequestBody UpdateDoctorStatusRequest request) {
        return R.ok(doctorTenantContextService.updateDoctorStatus(id, request));
    }

    /**
     * 绑定医生科室。
     *
     * @param id 医生编号
     * @param request 绑定请求
     * @return 绑定结果
     */
    @PostMapping("/doctors/{id}/departments")
    public R<DoctorDepartmentBindingVO> bindDoctorDepartment(
        @PathVariable Long id,
        @Valid @RequestBody BindDoctorDepartmentRequest request
    ) {
        return R.ok(doctorTenantContextService.bindDoctorDepartment(id, request));
    }

    /**
     * 查询排班列表。
     *
     * @return 排班列表
     */
    @GetMapping("/schedules")
    public R<List<ScheduleVO>> schedules() {
        return R.ok(doctorTenantContextService.listSchedules());
    }

    /**
     * 创建排班。
     *
     * @param request 创建排班请求
     * @return 排班展示对象
     */
    @PostMapping("/schedules")
    public R<ScheduleVO> createSchedule(@Valid @RequestBody CreateScheduleRequest request) {
        return R.ok(doctorTenantContextService.createSchedule(request));
    }

    /**
     * 计算挂号费。
     *
     * @param request 挂号费请求
     * @return 挂号费
     */
    @PostMapping("/appointment-fee/resolve")
    public R<BigDecimal> resolveAppointmentFee(@RequestBody ResolveAppointmentFeeRequest request) {
        return R.ok(appointmentFeePolicyService.resolve(request));
    }
}

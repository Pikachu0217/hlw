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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        log.info("开放科室资源，deptId={}，name={}", request.getDeptId(), request.getName());
        return R.ok(doctorTenantContextService.createDepartment(request));
    }

    /**
     * 更新科室扩展属性。
     *
     * @param id 系统部门编号
     * @param request 科室扩展请求
     * @return 科室展示对象
     */
    @PutMapping("/departments/{id}")
    public R<DepartmentVO> updateDepartment(@PathVariable Long id, @Valid @RequestBody CreateDepartmentRequest request) {
        log.info("更新科室扩展属性，deptId={}，name={}", id, request.getName());
        request.setDeptId(id);
        return R.ok(doctorTenantContextService.updateDepartmentExtension(id, request));
    }

    /**
     * 查询医生列表。
     *
     * @param deptId 科室编号
     * @return 医生列表
     */
    @GetMapping("/doctors")
    public R<List<DoctorVO>> doctors(@RequestParam(required = false) Long deptId) {
        log.info("查询医生列表，deptId={}", deptId);
        return R.ok(doctorTenantContextService.listDoctors(deptId));
    }

    /**
     * 查询医生科室绑定列表。
     *
     * @return 医生科室绑定列表
     */
    @GetMapping("/doctor-department-bindings")
    public R<List<DoctorDepartmentBindingVO>> doctorDepartmentBindings() {
        log.info("查询医生科室绑定列表");
        return R.ok(doctorTenantContextService.listDoctorDepartmentBindings());
    }

    /**
     * 查询医生详情。
     *
     * @param id 医生账号业务编号
     * @return 医生详情
     */
    @GetMapping("/doctors/{id}")
    public R<DoctorVO> doctorDetail(@PathVariable String id) {
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
        log.info("纳入医生资源，userId={}，title={}", request.getUserId(), request.getTitle());
        return R.ok(doctorTenantContextService.createDoctor(request));
    }

    /**
     * 更新医生扩展属性。
     *
     * @param id 医生账号业务编号
     * @param request 医生扩展请求
     * @return 医生展示对象
     */
    @PutMapping("/doctors/{id}")
    public R<DoctorVO> updateDoctor(@PathVariable String id, @RequestBody CreateDoctorRequest request) {
        log.info("更新医生扩展属性，userId={}，title={}", id, request.getTitle());
        return R.ok(doctorTenantContextService.updateDoctorExtension(id, request));
    }

    /**
     * 更新医生状态。
     *
     * @param id 医生账号业务编号
     * @param request 状态请求
     * @return 医生展示对象
     */
    @PutMapping("/doctors/{id}/status")
    public R<DoctorVO> updateDoctorStatus(@PathVariable String id, @Valid @RequestBody UpdateDoctorStatusRequest request) {
        log.info("更新医生接诊状态，userId={}，status={}", id, request.getStatus());
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
        log.info("绑定医生科室，doctorId={}，deptId={}", id, request.getDeptId());
        return R.ok(doctorTenantContextService.bindDoctorDepartment(id, request));
    }

    /**
     * 查询排班列表。
     *
     * @return 排班列表
     */
    @GetMapping("/schedules")
    public R<List<ScheduleVO>> schedules(
        @RequestParam(required = false) String scheduleDate,
        @RequestParam(required = false) Long doctorId,
        @RequestParam(required = false) Long deptId
    ) {
        log.info("查询排班列表，scheduleDate={}，doctorId={}，deptId={}", scheduleDate, doctorId, deptId);
        return R.ok(doctorTenantContextService.listSchedules(scheduleDate, doctorId, deptId));
    }

    /**
     * 创建排班。
     *
     * @param request 创建排班请求
     * @return 排班展示对象
     */
    @PostMapping("/schedules")
    public R<ScheduleVO> createSchedule(@Valid @RequestBody CreateScheduleRequest request) {
        log.info("创建医生排班，doctorId={}，deptId={}，scheduleDate={}，timeSlot={}",
            request.getDoctorId(), request.getDeptId(), request.getScheduleDate(), request.getTimeSlot());
        return R.ok(doctorTenantContextService.createSchedule(request));
    }

    /**
     * 更新排班。
     *
     * @param id 排班编号
     * @param request 更新排班请求
     * @return 排班展示对象
     */
    @PutMapping("/schedules/{id}")
    public R<ScheduleVO> updateSchedule(@PathVariable Long id, @Valid @RequestBody CreateScheduleRequest request) {
        log.info("更新排班，scheduleId={}，doctorId={}，deptId={}", id, request.getDoctorId(), request.getDeptId());
        return R.ok(doctorTenantContextService.updateSchedule(id, request));
    }

    /**
     * 删除排班。
     *
     * @param id 排班编号
     * @return 操作结果
     */
    @DeleteMapping("/schedules/{id}")
    public R<Void> deleteSchedule(@PathVariable Long id) {
        log.info("删除排班，scheduleId={}", id);
        doctorTenantContextService.deleteSchedule(id);
        return R.ok();
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

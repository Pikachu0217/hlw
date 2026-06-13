package com.hlw.doctor.controller;

import com.hlw.common.core.domain.R;
import com.hlw.common.core.jdbc.DemoDataQuery;
import com.hlw.doctor.service.AppointmentFeePolicy;
import com.hlw.doctor.service.DoctorDepartmentService;
import com.hlw.doctor.service.FeeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/doctor")
public class DoctorManagementController {
    private static final Logger log = LoggerFactory.getLogger(DoctorManagementController.class);

    private final AppointmentFeePolicy appointmentFeePolicy = new AppointmentFeePolicy();
    private final DemoDataQuery demoDataQuery;
    private final DoctorDepartmentService doctorDepartmentService;

    /**
     * 构造医生管理控制器。
     *
     * @param demoDataQuery 演示数据查询器
     * @param doctorDepartmentService 医生科室管理服务
     */
    public DoctorManagementController(DemoDataQuery demoDataQuery, DoctorDepartmentService doctorDepartmentService) {
        this.demoDataQuery = demoDataQuery;
        this.doctorDepartmentService = doctorDepartmentService;
    }

    /**
     * 查询科室演示列表。
     *
     * @return 科室列表
     */
    @GetMapping("/departments")
    public R<List<Map<String, Object>>> departments() {
        return R.ok(doctorDepartmentService.listDepartments());
    }

    /**
     * 创建科室。
     *
     * @param command 创建命令
     * @return 创建结果
     */
    @PostMapping("/departments")
    public R<Map<String, Object>> createDepartment(@RequestBody Map<String, Object> command) {
        return R.ok(doctorDepartmentService.createDepartment(command));
    }

    /**
     * 查询医生演示列表。
     *
     * @return 医生列表
     */
    @GetMapping("/doctors")
    public R<List<Map<String, Object>>> doctors() {
        log.info("查询医生列表");
        return R.ok(demoDataQuery.list("医生列表", doctorListSql() + " ORDER BY d.id"));
    }

    /**
     * 查询医生详情。
     *
     * @param id 医生编号
     * @return 医生详情
     */
    @GetMapping("/doctors/{id}")
    public R<Map<String, Object>> doctorDetail(@PathVariable Long id) {
        log.info("查询医生详情，doctorId={}", id);
        return R.ok(demoDataQuery.one("医生详情", doctorListSql() + " AND d.id = ?", id));
    }

    /**
     * 创建医生。
     *
     * @param command 创建命令
     * @return 创建结果
     */
    @PostMapping("/doctors")
    public R<Map<String, Object>> createDoctor(@RequestBody Map<String, Object> command) {
        return R.ok(command);
    }

    /**
     * 更新医生状态。
     *
     * @param id 医生编号
     * @param command 状态命令
     * @return 更新结果
     */
    @PutMapping("/doctors/{id}/status")
    public R<Map<String, Object>> updateDoctorStatus(@PathVariable Long id, @RequestBody Map<String, Object> command) {
        return R.ok(Map.of("id", id, "status", command.get("status")));
    }

    /**
     * 绑定医生科室。
     *
     * @param id 医生编号
     * @param command 绑定命令
     * @return 绑定结果
     */
    @PostMapping("/doctors/{id}/departments")
    public R<Map<String, Object>> bindDoctorDepartment(@PathVariable Long id, @RequestBody Map<String, Object> command) {
        return R.ok(doctorDepartmentService.bindDoctorDepartment(id, command));
    }

    /**
     * 查询排班演示列表。
     *
     * @return 排班列表
     */
    @GetMapping("/schedules")
    public R<List<Map<String, Object>>> schedules() {
        log.info("查询排班列表");
        return R.ok(demoDataQuery.list("排班列表", """
            SELECT s.id AS id,
                   s.doctor_id AS "doctorId",
                   d.doctor_name AS "doctorName",
                   s.slot AS slot,
                   s.remain_number AS remain
            FROM doc_schedule s
            LEFT JOIN doc_doctor d ON d.id = s.doctor_id AND d.deleted = 0
            WHERE s.deleted = 0
            ORDER BY s.id
            """));
    }

    /**
     * 创建排班。
     *
     * @param command 创建命令
     * @return 创建结果
     */
    @PostMapping("/schedules")
    public R<Map<String, Object>> createSchedule(@RequestBody Map<String, Object> command) {
        return R.ok(command);
    }

    /**
     * 计算挂号费。
     *
     * @param context 挂号费上下文
     * @return 挂号费
     */
    @PostMapping("/appointment-fee/resolve")
    public R<BigDecimal> resolveAppointmentFee(@RequestBody FeeContext context) {
        return R.ok(appointmentFeePolicy.resolve(context));
    }

    /**
     * 生成医生列表基础 SQL。
     *
     * @return 医生列表基础 SQL
     */
    private String doctorListSql() {
        return """
            SELECT d.id AS id,
                   d.id::text AS key,
                   d.doctor_name AS name,
                   d.title AS title,
                   d.department AS department,
                   d.specialty AS specialty,
                   d.status AS status,
                   d.consult_status AS "consultStatus",
                   d.schedule_desc AS schedule,
                   d.patient_count AS "patientCount",
                   to_char(d.consult_fee, 'FM999999990.00') AS "consultFee"
            FROM doc_doctor d
            WHERE d.deleted = 0
            """;
    }
}

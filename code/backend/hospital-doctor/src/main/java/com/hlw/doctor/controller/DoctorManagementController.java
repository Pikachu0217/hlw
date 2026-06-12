package com.hlw.doctor.controller;

import com.hlw.common.core.domain.R;
import com.hlw.doctor.service.AppointmentFeePolicy;
import com.hlw.doctor.service.FeeContext;
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

import static java.util.Map.entry;

@RestController
@RequestMapping("/doctor")
public class DoctorManagementController {
    private final AppointmentFeePolicy appointmentFeePolicy = new AppointmentFeePolicy();

    /**
     * 查询科室演示列表。
     *
     * @return 科室列表
     */
    @GetMapping("/departments")
    public R<List<Map<String, Object>>> departments() {
        return R.ok(List.of(
            Map.of("id", 10L, "name", "心内科", "doctorCount", 8),
            Map.of("id", 20L, "name", "儿科", "doctorCount", 12)
        ));
    }

    /**
     * 创建科室。
     *
     * @param command 创建命令
     * @return 创建结果
     */
    @PostMapping("/departments")
    public R<Map<String, Object>> createDepartment(@RequestBody Map<String, Object> command) {
        return R.ok(command);
    }

    /**
     * 查询医生演示列表。
     *
     * @return 医生列表
     */
    @GetMapping("/doctors")
    public R<List<Map<String, Object>>> doctors() {
        return R.ok(List.of(
            Map.ofEntries(
                entry("id", 1L),
                entry("key", "1"),
                entry("name", "陈知衡"),
                entry("title", "主任医师"),
                entry("department", "心内科"),
                entry("specialty", "冠脉慢病管理"),
                entry("status", "接诊中"),
                entry("consultStatus", "ONLINE"),
                entry("schedule", "上午门诊"),
                entry("patientCount", 16),
                entry("consultFee", "50.00")
            ),
            Map.ofEntries(
                entry("id", 2L),
                entry("key", "2"),
                entry("name", "顾清和"),
                entry("title", "副主任医师"),
                entry("department", "内分泌科"),
                entry("specialty", "糖尿病营养干预"),
                entry("status", "候诊"),
                entry("consultStatus", "BUSY"),
                entry("schedule", "下午门诊"),
                entry("patientCount", 9),
                entry("consultFee", "30.00")
            )
        ));
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
        return R.ok(Map.of("doctorId", id, "department", command));
    }

    /**
     * 查询排班演示列表。
     *
     * @return 排班列表
     */
    @GetMapping("/schedules")
    public R<List<Map<String, Object>>> schedules() {
        return R.ok(List.of(
            Map.of("id", 1L, "doctorId", 1L, "doctorName", "陈知衡", "slot", "上午", "remain", 6),
            Map.of("id", 2L, "doctorId", 2L, "doctorName", "顾清和", "slot", "下午", "remain", 1)
        ));
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
}

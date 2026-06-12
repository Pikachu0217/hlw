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

@RestController
@RequestMapping("/doctor")
public class DoctorManagementController {
    private final AppointmentFeePolicy appointmentFeePolicy = new AppointmentFeePolicy();

    @GetMapping("/departments")
    public R<List<Map<String, Object>>> departments() {
        return R.ok(List.of());
    }

    @PostMapping("/departments")
    public R<Map<String, Object>> createDepartment(@RequestBody Map<String, Object> command) {
        return R.ok(command);
    }

    @GetMapping("/doctors")
    public R<List<Map<String, Object>>> doctors() {
        return R.ok(List.of());
    }

    @PostMapping("/doctors")
    public R<Map<String, Object>> createDoctor(@RequestBody Map<String, Object> command) {
        return R.ok(command);
    }

    @PutMapping("/doctors/{id}/status")
    public R<Map<String, Object>> updateDoctorStatus(@PathVariable Long id, @RequestBody Map<String, Object> command) {
        return R.ok(Map.of("id", id, "status", command.get("status")));
    }

    @PostMapping("/doctors/{id}/departments")
    public R<Map<String, Object>> bindDoctorDepartment(@PathVariable Long id, @RequestBody Map<String, Object> command) {
        return R.ok(Map.of("doctorId", id, "department", command));
    }

    @GetMapping("/schedules")
    public R<List<Map<String, Object>>> schedules() {
        return R.ok(List.of());
    }

    @PostMapping("/schedules")
    public R<Map<String, Object>> createSchedule(@RequestBody Map<String, Object> command) {
        return R.ok(command);
    }

    @PostMapping("/appointment-fee/resolve")
    public R<BigDecimal> resolveAppointmentFee(@RequestBody FeeContext context) {
        return R.ok(appointmentFeePolicy.resolve(context));
    }
}

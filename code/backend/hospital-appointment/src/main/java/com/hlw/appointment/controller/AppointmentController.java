package com.hlw.appointment.controller;

import com.hlw.appointment.service.Appointment;
import com.hlw.appointment.service.GrabAppointmentService;
import com.hlw.appointment.service.NumberSource;
import com.hlw.appointment.service.NumberSourceService;
import com.hlw.common.core.domain.R;
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
    private final NumberSourceService numberSourceService;
    private final GrabAppointmentService grabAppointmentService;

    public AppointmentController(NumberSourceService numberSourceService, GrabAppointmentService grabAppointmentService) {
        this.numberSourceService = numberSourceService;
        this.grabAppointmentService = grabAppointmentService;
    }

    @GetMapping("/number-sources")
    public R<List<NumberSource>> numberSources() {
        return R.ok(List.of());
    }

    @PostMapping("/appointments")
    public R<Map<String, Object>> createAppointment(@RequestBody Map<String, Object> command) {
        return R.ok(command);
    }

    @PostMapping("/appointments/{id}/pay")
    public R<Map<String, Object>> pay(@PathVariable Long id) {
        return R.ok(Map.of("id", id, "status", "PAID"));
    }

    @PostMapping("/appointments/{id}/check-in")
    public R<Map<String, Object>> checkIn(@PathVariable Long id) {
        return R.ok(Map.of("id", id, "status", "CHECKED_IN"));
    }

    @PostMapping("/appointments/{id}/grab")
    public R<Boolean> grab(@PathVariable Long id, @RequestBody Map<String, Long> command) {
        return R.ok(grabAppointmentService.grab(id, command.get("doctorId")));
    }

    @PostMapping("/number-sources/{scheduleId}/lock")
    public R<NumberSource> lockNumberSource(@PathVariable Long scheduleId) {
        return R.ok(numberSourceService.lockOne(scheduleId));
    }

    @PostMapping("/release-configs")
    public R<Map<String, Object>> createReleaseConfig(@RequestBody Map<String, Object> command) {
        return R.ok(command);
    }
}

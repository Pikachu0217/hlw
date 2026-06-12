package com.hlw.patient.controller;

import com.hlw.common.core.domain.R;
import com.hlw.patient.service.PatientProfile;
import com.hlw.patient.service.PatientProfileService;
import com.hlw.patient.service.UpdatePatientProfileCommand;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/patient")
public class PatientProfileController {
    private final PatientProfileService patientProfileService;

    public PatientProfileController(PatientProfileService patientProfileService) {
        this.patientProfileService = patientProfileService;
    }

    @GetMapping("/profile")
    public R<PatientProfile> profile() {
        return R.ok(patientProfileService.findProfile(1L));
    }

    @PutMapping("/profile")
    public R<PatientProfile> updateProfile(@RequestBody UpdatePatientProfileCommand command) {
        return R.ok(patientProfileService.updateProfile(1L, command));
    }

    @GetMapping("/health-records")
    public R<List<Map<String, Object>>> healthRecords() {
        return R.ok(List.of());
    }

    @PostMapping("/health-records")
    public R<Map<String, Object>> createHealthRecord(@RequestBody Map<String, Object> command) {
        return R.ok(command);
    }
}

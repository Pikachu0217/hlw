package com.hlw.patient.service;

import java.util.HashMap;
import java.util.Map;

public class InMemoryPatientRepository implements PatientRepository {
    private final Map<Long, PatientProfile> profiles = new HashMap<>();

    @Override
    public PatientProfile save(Long patientId, UpdatePatientProfileCommand command, String maskedPhone) {
        PatientProfile profile = new PatientProfile(patientId, command.name(), maskedPhone, command.gender());
        profiles.put(patientId, profile);
        return profile;
    }

    @Override
    public PatientProfile findById(Long patientId) {
        return profiles.get(patientId);
    }
}

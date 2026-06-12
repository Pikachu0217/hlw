package com.hlw.patient.service;

public interface PatientRepository {
    PatientProfile save(Long patientId, UpdatePatientProfileCommand command, String maskedPhone);

    PatientProfile findById(Long patientId);
}

package com.hlw.patient.service;

public class PatientProfileService {
    private final PatientRepository patientRepository;

    public PatientProfileService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public PatientProfile findProfile(Long patientId) {
        return patientRepository.findById(patientId);
    }

    public PatientProfile updateProfile(Long patientId, UpdatePatientProfileCommand command) {
        return patientRepository.save(patientId, command, maskPhone(command.phone()));
    }

    public String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}

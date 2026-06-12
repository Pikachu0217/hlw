package com.hlw.appointment.service;

public interface AppointmentRepository {
    void save(Appointment appointment);

    Appointment findById(Long appointmentId);
}

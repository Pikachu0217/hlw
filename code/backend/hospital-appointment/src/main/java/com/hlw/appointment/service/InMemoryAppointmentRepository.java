package com.hlw.appointment.service;

import java.util.HashMap;
import java.util.Map;

public class InMemoryAppointmentRepository implements AppointmentRepository {
    private final Map<Long, Appointment> appointments = new HashMap<>();

    @Override
    public void save(Appointment appointment) {
        appointments.put(appointment.id(), appointment);
    }

    @Override
    public Appointment findById(Long appointmentId) {
        return appointments.get(appointmentId);
    }
}

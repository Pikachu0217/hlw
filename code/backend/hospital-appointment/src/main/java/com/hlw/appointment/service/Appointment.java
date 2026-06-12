package com.hlw.appointment.service;

public record Appointment(Long id, Long patientId, Long departmentId, Long doctorId, AppointmentStatus status) {
    public static Appointment convenient(Long id, Long patientId, Long departmentId) {
        return new Appointment(id, patientId, departmentId, null, AppointmentStatus.WAITING_GRAB);
    }

    public Appointment grabbedBy(Long doctorId) {
        return new Appointment(id, patientId, departmentId, doctorId, AppointmentStatus.GRABBED);
    }
}

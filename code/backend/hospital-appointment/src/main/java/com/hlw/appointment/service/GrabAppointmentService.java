package com.hlw.appointment.service;

public class GrabAppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final DistributedLock distributedLock;

    public GrabAppointmentService(AppointmentRepository appointmentRepository, DistributedLock distributedLock) {
        this.appointmentRepository = appointmentRepository;
        this.distributedLock = distributedLock;
    }

    public boolean grab(Long appointmentId, Long doctorId) {
        String grabLockKey = "hlw:grab:appointment:" + appointmentId;
        if (!distributedLock.tryLock(grabLockKey)) {
            return false;
        }
        Appointment appointment = appointmentRepository.findById(appointmentId);
        if (appointment == null || appointment.doctorId() != null) {
            return false;
        }
        appointmentRepository.save(appointment.grabbedBy(doctorId));
        return true;
    }
}

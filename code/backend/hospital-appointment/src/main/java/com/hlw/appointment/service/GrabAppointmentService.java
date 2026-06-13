package com.hlw.appointment.service;

public class GrabAppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final DistributedLock distributedLock;

    public GrabAppointmentService(AppointmentRepository appointmentRepository, DistributedLock distributedLock) {
        this.appointmentRepository = appointmentRepository;
        this.distributedLock = distributedLock;
    }

    /**
     * 抢占便民门诊预约单。
     *
     * @param appointmentId 预约编号
     * @param doctorId 医生编号
     * @return 是否抢单成功
     */
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

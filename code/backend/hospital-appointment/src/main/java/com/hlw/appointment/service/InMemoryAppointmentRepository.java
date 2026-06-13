package com.hlw.appointment.service;

import java.util.HashMap;
import java.util.Map;

public class InMemoryAppointmentRepository implements AppointmentRepository {
    private final Map<Long, Appointment> appointments = new HashMap<>();

    /**
     * 保存预约到内存仓储。
     *
     * @param appointment 预约记录
     */
    @Override
    public void save(Appointment appointment) {
        appointments.put(appointment.id(), appointment);
    }

    /**
     * 按编号查询内存预约。
     *
     * @param appointmentId 预约编号
     * @return 预约记录
     */
    @Override
    public Appointment findById(Long appointmentId) {
        return appointments.get(appointmentId);
    }
}

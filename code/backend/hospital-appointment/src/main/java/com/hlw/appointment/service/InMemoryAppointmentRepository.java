package com.hlw.appointment.service;

import java.util.HashMap;
import java.util.Map;

public class InMemoryAppointmentRepository implements AppointmentRepository {
    private final Map<Long, Appointment> appointments = new HashMap<>();

    @Override
    /**
     * 保存预约到内存仓储。
     *
     * @param appointment 预约记录
     */
    public void save(Appointment appointment) {
        appointments.put(appointment.id(), appointment);
    }

    @Override
    /**
     * 按编号查询内存预约。
     *
     * @param appointmentId 预约编号
     * @return 预约记录
     */
    public Appointment findById(Long appointmentId) {
        return appointments.get(appointmentId);
    }
}

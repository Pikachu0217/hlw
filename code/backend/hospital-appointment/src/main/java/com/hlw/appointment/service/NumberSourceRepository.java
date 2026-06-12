package com.hlw.appointment.service;

public interface NumberSourceRepository {
    void save(NumberSource numberSource);

    NumberSource findFirstAvailableByScheduleId(Long scheduleId);

    NumberSource findById(Long id);
}

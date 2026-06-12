package com.hlw.appointment.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class InMemoryNumberSourceRepository implements NumberSourceRepository {
    private final Map<Long, NumberSource> numberSources = new HashMap<>();

    @Override
    public void save(NumberSource numberSource) {
        numberSources.put(numberSource.id(), numberSource);
    }

    @Override
    public NumberSource findFirstAvailableByScheduleId(Long scheduleId) {
        return numberSources.values().stream()
            .filter(source -> source.scheduleId().equals(scheduleId))
            .filter(source -> source.status() == NumberSourceStatus.AVAILABLE)
            .min(Comparator.comparing(NumberSource::numberSeq))
            .orElse(null);
    }

    @Override
    public NumberSource findById(Long id) {
        return numberSources.get(id);
    }
}

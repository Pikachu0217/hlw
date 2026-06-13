package com.hlw.appointment.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class InMemoryNumberSourceRepository implements NumberSourceRepository {
    private final Map<Long, NumberSource> numberSources = new HashMap<>();

    /**
     * 保存号源到内存仓储。
     *
     * @param numberSource 号源记录
     */
    @Override
    public void save(NumberSource numberSource) {
        numberSources.put(numberSource.id(), numberSource);
    }

    /**
     * 查询指定排班下首个可用号源。
     *
     * @param scheduleId 排班编号
     * @return 可用号源
     */
    @Override
    public NumberSource findFirstAvailableByScheduleId(Long scheduleId) {
        return numberSources.values().stream()
            .filter(source -> source.scheduleId().equals(scheduleId))
            .filter(source -> source.status() == NumberSourceStatus.AVAILABLE)
            .min(Comparator.comparing(NumberSource::numberSeq))
            .orElse(null);
    }

    /**
     * 按编号查询号源。
     *
     * @param id 号源编号
     * @return 号源记录
     */
    @Override
    public NumberSource findById(Long id) {
        return numberSources.get(id);
    }
}

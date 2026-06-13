package com.hlw.appointment.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class InMemoryNumberSourceRepository implements NumberSourceRepository {
    private final Map<Long, NumberSource> numberSources = new HashMap<>();

    @Override
    /**
     * 保存号源到内存仓储。
     *
     * @param numberSource 号源记录
     */
    public void save(NumberSource numberSource) {
        numberSources.put(numberSource.id(), numberSource);
    }

    @Override
    /**
     * 查询指定排班下首个可用号源。
     *
     * @param scheduleId 排班编号
     * @return 可用号源
     */
    public NumberSource findFirstAvailableByScheduleId(Long scheduleId) {
        return numberSources.values().stream()
            .filter(source -> source.scheduleId().equals(scheduleId))
            .filter(source -> source.status() == NumberSourceStatus.AVAILABLE)
            .min(Comparator.comparing(NumberSource::numberSeq))
            .orElse(null);
    }

    @Override
    /**
     * 按编号查询号源。
     *
     * @param id 号源编号
     * @return 号源记录
     */
    public NumberSource findById(Long id) {
        return numberSources.get(id);
    }
}

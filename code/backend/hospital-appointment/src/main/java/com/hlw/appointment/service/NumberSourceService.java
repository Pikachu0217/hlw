package com.hlw.appointment.service;

import com.hlw.common.core.exception.BizException;

public class NumberSourceService {
    private final NumberSourceRepository numberSourceRepository;
    private final DistributedLock distributedLock;

    public NumberSourceService(NumberSourceRepository numberSourceRepository, DistributedLock distributedLock) {
        this.numberSourceRepository = numberSourceRepository;
        this.distributedLock = distributedLock;
    }

    public NumberSource lockOne(Long scheduleId) {
        String numberLockKey = "hlw:lock:number:" + scheduleId;
        if (!distributedLock.tryLock(numberLockKey)) {
            throw new BizException(409, "号源已被锁定");
        }
        NumberSource source = numberSourceRepository.findFirstAvailableByScheduleId(scheduleId);
        if (source == null) {
            throw new BizException(404, "暂无可用号源");
        }
        NumberSource locked = source.locked();
        numberSourceRepository.save(locked);
        return locked;
    }

    public boolean tryLockSameNumberAgain(Long numberSourceId) {
        NumberSource source = numberSourceRepository.findById(numberSourceId);
        return source != null && source.status() == NumberSourceStatus.AVAILABLE;
    }
}

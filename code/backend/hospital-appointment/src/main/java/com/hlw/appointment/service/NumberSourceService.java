package com.hlw.appointment.service;

import com.hlw.common.core.exception.BizException;

public class NumberSourceService {
    private final NumberSourceRepository numberSourceRepository;
    private final DistributedLock distributedLock;

    public NumberSourceService(NumberSourceRepository numberSourceRepository, DistributedLock distributedLock) {
        this.numberSourceRepository = numberSourceRepository;
        this.distributedLock = distributedLock;
    }

    /**
     * 锁定指定排班下的首个可用号源。
     *
     * @param scheduleId 排班编号
     * @return 已锁定号源
     */
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

    /**
     * 判断号源是否仍可被再次锁定。
     *
     * @param numberSourceId 号源编号
     * @return 是否仍处于可用状态
     */
    public boolean tryLockSameNumberAgain(Long numberSourceId) {
        NumberSource source = numberSourceRepository.findById(numberSourceId);
        return source != null && source.status() == NumberSourceStatus.AVAILABLE;
    }
}

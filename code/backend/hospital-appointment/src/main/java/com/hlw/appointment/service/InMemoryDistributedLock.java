package com.hlw.appointment.service;

import java.util.HashSet;
import java.util.Set;

public class InMemoryDistributedLock implements DistributedLock {
    private final Set<String> lockedKeys = new HashSet<>();

    /**
     * 尝试获取内存锁。
     *
     * @param key 锁键
     * @return 是否获取成功
     */
    @Override
    public boolean tryLock(String key) {
        return lockedKeys.add(key);
    }
}

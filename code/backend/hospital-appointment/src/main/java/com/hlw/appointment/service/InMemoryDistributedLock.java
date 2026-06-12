package com.hlw.appointment.service;

import java.util.HashSet;
import java.util.Set;

public class InMemoryDistributedLock implements DistributedLock {
    private final Set<String> lockedKeys = new HashSet<>();

    @Override
    public boolean tryLock(String key) {
        return lockedKeys.add(key);
    }
}

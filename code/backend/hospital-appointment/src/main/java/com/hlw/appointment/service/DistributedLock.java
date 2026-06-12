package com.hlw.appointment.service;

public interface DistributedLock {
    boolean tryLock(String key);
}

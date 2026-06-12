package com.hlw.consult.service;

@FunctionalInterface
public interface ConsultDurationProvider {
    int durationMinutes(Long tenantId);
}

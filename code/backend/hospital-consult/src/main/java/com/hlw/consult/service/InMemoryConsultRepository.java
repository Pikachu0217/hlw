package com.hlw.consult.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryConsultRepository implements ConsultRepository {
    private final Map<Long, Consult> consults = new HashMap<>();

    @Override
    public void save(Consult consult) {
        consults.put(consult.id(), consult);
    }

    @Override
    public Consult findById(Long consultId) {
        return consults.get(consultId);
    }

    @Override
    public List<Consult> findInProgress() {
        return consults.values().stream()
            .filter(consult -> consult.status() == ConsultStatus.IN_PROGRESS)
            .toList();
    }
}

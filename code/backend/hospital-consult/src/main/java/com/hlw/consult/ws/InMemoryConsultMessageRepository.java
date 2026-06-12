package com.hlw.consult.ws;

import java.util.ArrayList;
import java.util.List;

public class InMemoryConsultMessageRepository implements ConsultMessageRepository {
    private final List<ConsultMessage> messages = new ArrayList<>();

    @Override
    public void save(ConsultMessage message) {
        messages.add(message);
    }

    @Override
    public List<ConsultMessage> findByConsultId(Long consultId) {
        return messages.stream()
            .filter(message -> message.consultId().equals(consultId))
            .toList();
    }
}

package com.hlw.consult.ws;

import java.util.List;

public interface ConsultMessageRepository {
    void save(ConsultMessage message);

    List<ConsultMessage> findByConsultId(Long consultId);
}

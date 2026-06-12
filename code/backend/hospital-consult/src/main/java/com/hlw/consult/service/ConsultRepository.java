package com.hlw.consult.service;

import java.util.List;

public interface ConsultRepository {
    void save(Consult consult);

    Consult findById(Long consultId);

    List<Consult> findInProgress();
}

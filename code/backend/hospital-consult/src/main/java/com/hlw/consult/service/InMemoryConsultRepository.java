package com.hlw.consult.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryConsultRepository implements ConsultRepository {
    private final Map<Long, Consult> consults = new HashMap<>();

    @Override
    /**
     * 保存问诊到内存仓储。
     *
     * @param consult 问诊记录
     */
    public void save(Consult consult) {
        consults.put(consult.id(), consult);
    }

    @Override
    /**
     * 按编号查询问诊。
     *
     * @param consultId 问诊编号
     * @return 问诊记录
     */
    public Consult findById(Long consultId) {
        return consults.get(consultId);
    }

    @Override
    /**
     * 查询咨询中的问诊列表。
     *
     * @return 咨询中问诊列表
     */
    public List<Consult> findInProgress() {
        return consults.values().stream()
            .filter(consult -> consult.status() == ConsultStatus.IN_PROGRESS)
            .toList();
    }
}

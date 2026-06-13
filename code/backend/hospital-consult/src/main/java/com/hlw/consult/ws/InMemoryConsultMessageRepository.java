package com.hlw.consult.ws;

import java.util.ArrayList;
import java.util.List;

public class InMemoryConsultMessageRepository implements ConsultMessageRepository {
    private final List<ConsultMessage> messages = new ArrayList<>();

    @Override
    /**
     * 保存问诊消息到内存仓储。
     *
     * @param message 问诊消息
     */
    public void save(ConsultMessage message) {
        messages.add(message);
    }

    @Override
    /**
     * 查询指定问诊的消息列表。
     *
     * @param consultId 问诊编号
     * @return 问诊消息列表
     */
    public List<ConsultMessage> findByConsultId(Long consultId) {
        return messages.stream()
            .filter(message -> message.consultId().equals(consultId))
            .toList();
    }
}

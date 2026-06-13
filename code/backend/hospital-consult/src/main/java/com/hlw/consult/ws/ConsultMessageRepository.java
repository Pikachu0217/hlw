package com.hlw.consult.ws;

import java.util.List;

/**
 * 问诊消息仓储接口。
 */
public interface ConsultMessageRepository {
    /**
     * 保存问诊消息。
     *
     * @param message 问诊消息
     */
    void save(ConsultMessage message);

    /**
     * 查询问诊消息列表。
     *
     * @param consultId 问诊编号
     * @return 问诊消息列表
     */
    List<ConsultMessage> findByConsultId(Long consultId);
}

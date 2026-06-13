package com.hlw.consult.service;

public class ConsultLifecycleService {
    private final ConsultRepository consultRepository;
    private final ConsultDurationProvider durationProvider;

    public ConsultLifecycleService(ConsultRepository consultRepository, ConsultDurationProvider durationProvider) {
        this.consultRepository = consultRepository;
        this.durationProvider = durationProvider;
    }

    /**
     * 接单并将问诊切换为咨询中。
     *
     * @param consultId 问诊编号
     * @param tenantId 租户编号
     * @return 接单后的问诊
     */
    public Consult accept(Long consultId, Long tenantId) {
        Consult existing = consultRepository.findById(consultId);
        Consult consult = existing == null
            ? new Consult(consultId, tenantId, ConsultStatus.WAITING, 0, 0)
            : existing;
        Consult accepted = consult.inProgress(durationProvider.durationMinutes(tenantId));
        consultRepository.save(accepted);
        return accepted;
    }
}

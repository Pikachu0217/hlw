package com.hlw.consult.service;

public class ConsultLifecycleService {
    private final ConsultRepository consultRepository;
    private final ConsultDurationProvider durationProvider;

    public ConsultLifecycleService(ConsultRepository consultRepository, ConsultDurationProvider durationProvider) {
        this.consultRepository = consultRepository;
        this.durationProvider = durationProvider;
    }

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

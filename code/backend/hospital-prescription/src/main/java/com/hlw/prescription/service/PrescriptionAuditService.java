package com.hlw.prescription.service;

import com.hlw.common.mq.core.MqProducer;
import com.hlw.common.mq.model.MqMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 处方审核服务，负责审核结果落库并发送审核完成事件。
 */
public class PrescriptionAuditService {
    private static final Logger log = LoggerFactory.getLogger(PrescriptionAuditService.class);

    private final PrescriptionRepository prescriptionRepository;
    private final MqProducer mqProducer;

    /**
     * 构造处方审核服务。
     *
     * @param prescriptionRepository 处方仓储
     * @param mqProducer 消息生产者
     */
    public PrescriptionAuditService(PrescriptionRepository prescriptionRepository, MqProducer mqProducer) {
        this.prescriptionRepository = prescriptionRepository;
        this.mqProducer = mqProducer;
    }

    /**
     * 审核通过处方并发布审核完成事件。
     *
     * @param prescriptionId 处方编号
     * @param pharmacistId 审核药师编号
     * @param auditRemark 审核备注
     * @return 审核后的处方
     */
    public Prescription approve(Long prescriptionId, Long pharmacistId, String auditRemark) {
        log.info("处方审核通过，prescriptionId={}, pharmacistId={}, auditRemark={}", prescriptionId, pharmacistId, auditRemark);
        Prescription prescription = prescriptionRepository.findById(prescriptionId).audited(pharmacistId, auditRemark);
        prescriptionRepository.save(prescription);
        mqProducer.publish(new MqMessage("prescription.audited", "{\"prescriptionId\":" + prescriptionId + "}", 0, 0, 3));
        log.info("处方审核完成事件已发布，prescriptionId={}", prescriptionId);
        return prescription;
    }
}

package com.hlw.prescription.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 处方审核服务测试。
 */
class PrescriptionAuditServiceTest {
    /**
     * 验证处方审核通过后会发送审核完成事件。
     */
    @Test
    void audited_prescription_publishes_order_creation_event() {
        RecordingMqProducer producer = new RecordingMqProducer();
        PrescriptionAuditService service = new PrescriptionAuditService(new InMemoryPrescriptionRepository(), producer);

        Prescription prescription = service.approve(1L, 9L, "审核通过");

        assertThat(prescription.status()).isEqualTo(PrescriptionStatus.AUDITED);
        assertThat(producer.lastTopic()).isEqualTo("prescription.audited");
    }
}

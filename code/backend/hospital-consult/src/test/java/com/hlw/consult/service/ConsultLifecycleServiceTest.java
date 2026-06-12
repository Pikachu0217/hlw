package com.hlw.consult.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsultLifecycleServiceTest {
    @Test
    void accepting_consult_sets_duration_from_tenant_config() {
        ConsultLifecycleService service = new ConsultLifecycleService(
            new InMemoryConsultRepository(),
            tenantId -> 30
        );

        Consult consult = service.accept(1L, 100L);

        assertThat(consult.status()).isEqualTo(ConsultStatus.IN_PROGRESS);
        assertThat(consult.durationLimit()).isEqualTo(30);
        assertThat(consult.remainingSeconds()).isEqualTo(1800);
    }
}

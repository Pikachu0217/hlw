package com.hlw.appointment.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NumberSourceServiceTest {
    @Test
    void lock_and_use_available_number_source_once() {
        InMemoryNumberSourceRepository repository = new InMemoryNumberSourceRepository();
        repository.save(new NumberSource(1L, 10L, 1, NumberSourceStatus.AVAILABLE));
        NumberSourceService service = new NumberSourceService(repository, new InMemoryDistributedLock());

        NumberSource locked = service.lockOne(10L);

        assertThat(locked.status()).isEqualTo(NumberSourceStatus.LOCKED);
        assertThat(service.tryLockSameNumberAgain(1L)).isFalse();
    }
}

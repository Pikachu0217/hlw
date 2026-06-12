package com.hlw.appointment.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GrabAppointmentServiceTest {
    @Test
    void only_first_doctor_can_grab_convenient_appointment() {
        InMemoryAppointmentRepository repository = new InMemoryAppointmentRepository();
        repository.save(Appointment.convenient(1L, 100L, 20L));
        GrabAppointmentService service = new GrabAppointmentService(repository, new InMemoryDistributedLock());

        boolean first = service.grab(1L, 200L);
        boolean second = service.grab(1L, 201L);

        assertThat(first).isTrue();
        assertThat(second).isFalse();
        assertThat(repository.findById(1L).doctorId()).isEqualTo(200L);
    }
}

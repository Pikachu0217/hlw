package com.hlw.appointment.config;

import com.hlw.appointment.service.Appointment;
import com.hlw.appointment.service.AppointmentRepository;
import com.hlw.appointment.service.DistributedLock;
import com.hlw.appointment.service.GrabAppointmentService;
import com.hlw.appointment.service.InMemoryAppointmentRepository;
import com.hlw.appointment.service.InMemoryDistributedLock;
import com.hlw.appointment.service.InMemoryNumberSourceRepository;
import com.hlw.appointment.service.NumberSource;
import com.hlw.appointment.service.NumberSourceRepository;
import com.hlw.appointment.service.NumberSourceService;
import com.hlw.appointment.service.NumberSourceStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 预约模块本地启动默认配置。
 */
@Configuration
public class AppointmentServiceConfig {
    /**
     * 创建号源锁定服务。
     *
     * @param numberSourceRepository 号源仓储
     * @param distributedLock 分布式锁
     * @return 号源服务
     */
    @Bean
    public NumberSourceService numberSourceService(
        NumberSourceRepository numberSourceRepository,
        DistributedLock distributedLock
    ) {
        return new NumberSourceService(numberSourceRepository, distributedLock);
    }

    /**
     * 创建便民门诊抢单服务。
     *
     * @param appointmentRepository 预约仓储
     * @param distributedLock 分布式锁
     * @return 抢单服务
     */
    @Bean
    public GrabAppointmentService grabAppointmentService(
        AppointmentRepository appointmentRepository,
        DistributedLock distributedLock
    ) {
        return new GrabAppointmentService(appointmentRepository, distributedLock);
    }

    /**
     * 创建带演示数据的号源仓储。
     *
     * @return 号源仓储
     */
    @Bean
    public NumberSourceRepository numberSourceRepository() {
        InMemoryNumberSourceRepository repository = new InMemoryNumberSourceRepository();
        repository.save(new NumberSource(1L, 1L, 1, NumberSourceStatus.AVAILABLE));
        repository.save(new NumberSource(2L, 1L, 2, NumberSourceStatus.AVAILABLE));
        return repository;
    }

    /**
     * 创建带演示数据的预约仓储。
     *
     * @return 预约仓储
     */
    @Bean
    public AppointmentRepository appointmentRepository() {
        InMemoryAppointmentRepository repository = new InMemoryAppointmentRepository();
        repository.save(Appointment.convenient(1L, 1L, 20L));
        return repository;
    }

    /**
     * 创建内存锁实现。
     *
     * @return 分布式锁
     */
    @Bean
    public DistributedLock distributedLock() {
        return new InMemoryDistributedLock();
    }
}

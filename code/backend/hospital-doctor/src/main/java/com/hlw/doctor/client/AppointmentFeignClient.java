package com.hlw.doctor.client;

import com.hlw.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 预约服务内部 Feign 客户端。
 */
@FeignClient(name = "hospital-appointment", contextId = "doctorAppointmentFeignClient")
public interface AppointmentFeignClient {

    /**
     * 内部接口：根据排班编号自动创建放号配置和号源。
     *
     * @param request 包含 scheduleId 和 releaseCount
     * @return 放号配置结果
     */
    @PostMapping("/appointment/internal/release-configs")
    R<Void> createReleaseConfig(@RequestBody InternalCreateReleaseConfigRequest request);
}

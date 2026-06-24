package com.hlw.consult.client;

import com.hlw.common.core.domain.R;
import com.hlw.consult.client.resp.InternalAppointmentResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 预约服务内部 Feign 客户端。
 */
@FeignClient(name = "hospital-appointment", contextId = "consultAppointmentFeignClient")
public interface AppointmentFeignClient {

    /**
     * 内部查询预约单基本信息。
     *
     * @param id 预约单编号
     * @return 预约单内部响应
     */
    @GetMapping("/appointment/internal/appointments/{id}")
    R<InternalAppointmentResp> getAppointment(@PathVariable("id") Long id);
}

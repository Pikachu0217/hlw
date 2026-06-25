package com.hlw.appointment.client;

import com.hlw.appointment.client.req.InternalCreateConsultFromAppointmentRequest;
import com.hlw.appointment.client.req.InternalSyncConsultStatusRequest;
import com.hlw.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 问诊服务内部 Feign 客户端。
 */
@FeignClient(name = "hospital-consult", contextId = "appointmentConsultFeignClient")
public interface ConsultFeignClient {

    /**
     * 内部创建预约绑定问诊单。
     *
     * @param request 创建请求
     * @return 创建结果
     */
    @PostMapping("/consult/internal/consults/from-appointment")
    R<Void> createFromAppointment(@RequestBody InternalCreateConsultFromAppointmentRequest request);

    /**
     * 内部同步问诊状态。
     *
     * @param request 同步请求
     * @return 同步结果
     */
    @PostMapping("/consult/internal/consults/sync-status")
    R<Void> syncStatus(@RequestBody InternalSyncConsultStatusRequest request);
}

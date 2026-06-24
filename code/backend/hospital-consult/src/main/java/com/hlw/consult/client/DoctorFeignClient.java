package com.hlw.consult.client;

import com.hlw.common.core.domain.R;
import com.hlw.consult.client.resp.InternalDoctorResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 医生服务内部 Feign 客户端。
 */
@FeignClient(name = "hospital-doctor", contextId = "consultDoctorFeignClient")
public interface DoctorFeignClient {
    /**
     * 按租户和登录用户查询医生档案。
     *
     * @param tenantId 租户编号
     * @param userId 登录用户业务编号
     * @return 医生档案
     */
    @GetMapping("/internal/doctors/by-user")
    R<InternalDoctorResp> findByUser(@RequestParam("tenantId") Long tenantId, @RequestParam("userId") String userId);
}

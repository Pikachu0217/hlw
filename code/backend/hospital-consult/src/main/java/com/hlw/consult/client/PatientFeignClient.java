package com.hlw.consult.client;

import com.hlw.common.core.domain.R;
import com.hlw.consult.client.resp.InternalPatientResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 患者服务内部 Feign 客户端。
 */
@FeignClient(name = "hospital-patient", contextId = "consultPatientFeignClient")
public interface PatientFeignClient {
    /**
     * 按租户和登录用户查询患者档案。
     *
     * @param tenantId 租户编号
     * @param userId 登录用户编号
     * @return 患者档案
     */
    @GetMapping("/internal/patients/by-user")
    R<InternalPatientResp> findByUser(@RequestParam("tenantId") Long tenantId, @RequestParam("userId") Long userId);
}

package com.hlw.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * hospital-patient 内部患者 Feign 客户端。
 */
@FeignClient(name = "hospital-patient", contextId = "patientFeignClient")
public interface PatientFeignClient {

    /**
     * 创建或获取患者档案。
     *
     * @param req 请求体（tenantId、userId、phone）
     * @return 内部患者档案
     */
    @PostMapping("/internal/patients")
    com.hlw.common.core.domain.R<InternalPatientFeignResp> createOrGetByUser(@RequestBody CreatePatientFeignReq req);
}

package com.hlw.doctor.client;

import com.hlw.common.core.domain.R;
import com.hlw.common.core.domain.system.resp.InternalDeptResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * hospital-system 内部部门 Feign 客户端。
 */
@FeignClient(name = "hospital-system", contextId = "doctorSystemDeptFeignClient")
public interface SystemDeptFeignClient {
    /**
     * 查询租户科室部门列表。
     *
     * @param tenantId 租户编号
     * @return 科室部门列表
     */
    @GetMapping("/internal/depts")
    R<List<InternalDeptResp>> listDepartments(@RequestParam("tenantId") Long tenantId);

    /**
     * 查询租户部门详情。
     *
     * @param id 部门编号
     * @param tenantId 租户编号
     * @return 部门详情
     */
    @GetMapping("/internal/depts/{id}")
    R<InternalDeptResp> detail(@PathVariable("id") Long id, @RequestParam("tenantId") Long tenantId);

}

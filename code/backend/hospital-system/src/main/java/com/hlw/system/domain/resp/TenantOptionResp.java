package com.hlw.system.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 登录前租户选项展示对象，仅暴露租户选择所需的最小字段。
 */
@Getter
@Setter
public class TenantOptionResp {
    /** 主键编号。 */
    private Long id;
    /** 租户编号。 */
    private String tenantId;
    /** 企业名称。 */
    private String companyName;
    /** 租户状态。 */
    private String status;
}

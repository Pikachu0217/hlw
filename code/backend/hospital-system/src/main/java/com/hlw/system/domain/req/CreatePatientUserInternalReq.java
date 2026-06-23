package com.hlw.system.domain.req;

import lombok.Getter;
import lombok.Setter;

/**
 * 内部创建患者用户请求。
 */
@Getter
@Setter
public class CreatePatientUserInternalReq {
    /** 租户编号。 */
    private Long tenantId;
    /** 登录账号。 */
    private String userName;
    /** 联系电话。 */
    private String phone;
}

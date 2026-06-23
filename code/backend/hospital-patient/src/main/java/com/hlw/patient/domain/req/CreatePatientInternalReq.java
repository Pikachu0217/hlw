package com.hlw.patient.domain.req;

import lombok.Getter;
import lombok.Setter;

/**
 * 内部创建患者档案请求。
 */
@Getter
@Setter
public class CreatePatientInternalReq {
    /** 租户编号。 */
    private Long tenantId;
    /** 关联用户编号（sys_user.user_id 字符串）。 */
    private String userId;
    /** 联系电话。 */
    private String phone;
}

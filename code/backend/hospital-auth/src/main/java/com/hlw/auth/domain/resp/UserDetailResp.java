package com.hlw.auth.domain.resp;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 登录用户资料展示对象。
 */
@Getter
@Setter
public class UserDetailResp {
    /** 主键编号。 */
    private Long id;
    /** 用户业务编号。 */
    private String userId;
    /** 兼容字段：用户业务编号。 */
    private String businessUserId;
    /** 租户编号。 */
    private Long tenantId;
    /** 登录账号。 */
    private String username;
    /** 真实姓名。 */
    private String realName;
    /** 联系电话。 */
    private String phone;
    /** 用户类型。 */
    private String userType;
    /** 角色编码。 */
    private String roleCode;
    /** 资源列表。 */
    private List<String> resourceRoutePathList;
    /** 账号状态。 */
    private String status;
}

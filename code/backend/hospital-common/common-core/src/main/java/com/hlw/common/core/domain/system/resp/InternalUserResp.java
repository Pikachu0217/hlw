package com.hlw.common.core.domain.system.resp;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 从 hospital-system 内部接口接收的用户数据传输对象。
 */
@Getter
@Setter
public class InternalUserResp {
    /** 用户编号。 */
    private Long id;
    /** 租户编号。 */
    private Long tenantId;
    /** 登录账号。 */
    private String username;
    /** 登录密码哈希。 */
    private String password;
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

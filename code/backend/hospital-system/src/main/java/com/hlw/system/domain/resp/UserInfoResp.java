package com.hlw.system.domain.resp;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 当前登录用户信息展示对象。
 */
@Getter
@Setter
public class UserInfoResp {
    /** 用户信息。 */
    private UserResp user;
    /** 角色编码列表。 */
    private List<String> roles;
    /** 权限标识列表。 */
    private List<String> permissions;
}

package com.hlw.system.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户展示对象。
 */
@Getter
@Setter
public class UserResp {
    /** 表格主键。 */
    private String key;
    /** 用户名称。 */
    private String username;
    /** 部门编号。 */
    private Long deptId;
    /** 部门名称。 */
    private String deptName;
    /** 角色名称。 */
    private String roleName;
    /** 岗位名称。 */
    private String postName;
    /** 联系电话。 */
    private String phone;
    /** 最近登录时间。 */
    private String lastLogin;
    /** 状态。 */
    private String status;
}

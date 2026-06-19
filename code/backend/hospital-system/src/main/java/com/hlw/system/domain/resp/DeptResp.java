package com.hlw.system.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 部门展示对象。
 */
@Getter
@Setter
public class DeptResp {    /** 主键编号。 */
    private Long id;
    /** 父部门编号。 */
    private Long parentId;
    /** 部门名称。 */
    private String deptName;
    /** 祖级列表。 */
    private String ancestors;
    /** 显示顺序。 */
    private Integer orderNum;
    /** 负责人用户ID。 */
    private String leader;
    /** 联系电话。 */
    private String phone;
    /** 邮箱。 */
    private String email;
    /** 状态。 */
    private Integer status;
}

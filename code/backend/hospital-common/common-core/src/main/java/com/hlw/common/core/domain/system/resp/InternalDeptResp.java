package com.hlw.common.core.domain.system.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * hospital-system 内部部门数据传输对象。
 */
@Getter
@Setter
public class InternalDeptResp {
    /** 部门编号。 */
    private Long id;
    /** 租户编号。 */
    private Long tenantId;
    /** 父部门编号。 */
    private Long parentId;
    /** 部门名称。 */
    private String deptName;
    /** 是否科室（0=否，1=是）。 */
    private Integer isDepartment;
    /** 祖级列表。 */
    private String ancestors;
    /** 显示排序。 */
    private Integer orderNum;
    /** 部门状态。 */
    private Integer status;
}

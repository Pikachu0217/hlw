package com.hlw.common.core.domain.system.req;

import lombok.Getter;
import lombok.Setter;

/**
 * hospital-system 内部创建部门请求。
 */
@Getter
@Setter
public class InternalCreateDeptReq {
    /** 租户编号。 */
    private Long tenantId;
    /** 父部门编号。 */
    private Long parentId;
    /** 部门名称。 */
    private String deptName;
    /** 是否科室（0=否，1=是）。 */
    private Integer isDepartment;
    /** 显示排序。 */
    private Integer orderNum;
    /** 部门状态。 */
    private Integer status;
}

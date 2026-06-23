package com.hlw.doctor.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 科室展示对象。
 */
@Getter
@Setter
public class DepartmentVO {
    /** 系统部门编号。 */
    private Long id;
    /** 系统部门编号。 */
    private Long deptId;
    /** 科室扩展编号。 */
    private Long departmentId;
    /** 科室名称。 */
    private String name;
    /** 医生数量。 */
    private Integer doctorCount;
    /** 候诊描述。 */
    private String queue;
    /** 科室状态。 */
    private String status;
    /** 是否已维护扩展信息。 */
    private Boolean configured;
}

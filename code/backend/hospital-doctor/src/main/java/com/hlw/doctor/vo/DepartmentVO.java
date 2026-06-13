package com.hlw.doctor.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 科室展示对象。
 */
@Getter
@Setter
public class DepartmentVO {
    /** 表格主键。 */
    private String key;
    /** 科室编号。 */
    private Long id;
    /** 科室名称。 */
    private String name;
    /** 医生数量。 */
    private Integer doctorCount;
    /** 候诊描述。 */
    private String queue;
    /** 科室状态。 */
    private String status;
}

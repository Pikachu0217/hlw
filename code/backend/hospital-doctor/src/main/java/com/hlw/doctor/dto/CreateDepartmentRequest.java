package com.hlw.doctor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建科室请求。
 */
@Getter
@Setter
public class CreateDepartmentRequest {
    /** 科室名称。 */
    @NotBlank(message = "科室名称不能为空")
    private String name;
    /** 候诊描述。 */
    private String queue;
    /** 科室状态。 */
    private String status;
    /** 科室排序。 */
    private Integer sort;
    /** 科室说明。 */
    private String description;
    /** 父级科室编号。 */
    private Long parentId;
}

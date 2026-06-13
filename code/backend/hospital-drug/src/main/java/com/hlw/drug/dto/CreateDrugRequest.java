package com.hlw.drug.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建药品资料请求。
 */
@Getter
@Setter
public class CreateDrugRequest {
    /** 药品名称。 */
    @NotBlank(message = "药品名称不能为空")
    private String drugName;
    /** 药品规格。 */
    @NotBlank(message = "药品规格不能为空")
    private String spec;
    /** 初始库存。 */
    @Min(value = 0, message = "初始库存不能小于 0")
    private Integer inventory;
    /** 库存单位。 */
    private String unit;
}

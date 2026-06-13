package com.hlw.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建字典请求。
 */
@Getter
@Setter
public class CreateDictRequest {
    /** 字典类型。 */
    @NotBlank(message = "字典类型不能为空")
    private String dictType;
    /** 字典标签。 */
    @NotBlank(message = "字典标签不能为空")
    private String dictLabel;
    /** 字典键值。 */
    @NotBlank(message = "字典键值不能为空")
    private String dictValue;
    /** 排序。 */
    private Integer sort;
    /** 状态。 */
    private String status;
    /** 备注。 */
    private String remark;
}

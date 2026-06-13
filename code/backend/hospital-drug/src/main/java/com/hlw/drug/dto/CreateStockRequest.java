package com.hlw.drug.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建库存记录请求。
 */
@Getter
@Setter
public class CreateStockRequest {
    /** 药品编号。 */
    @NotNull(message = "药品编号不能为空")
    private Long drugId;
    /** 仓库名称。 */
    @NotBlank(message = "仓库名称不能为空")
    private String warehouseName;
    /** 库存数量。 */
    @Min(value = 0, message = "库存数量不能小于 0")
    private Integer inventory;
}

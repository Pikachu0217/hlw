package com.hlw.system.domain.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建岗位请求。
 */
@Getter
@Setter
public class CreatePostReq {
    /** 岗位名称。 */
    @NotBlank(message = "岗位名称不能为空")
    private String postName;
    /** 岗位编码。 */
    @NotBlank(message = "岗位编码不能为空")
    private String postCode;
    /** 排序。 */
    private Integer sort;
    /** 状态。 */
    private String status;
    /** 备注。 */
    private String remark;
}

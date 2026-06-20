package com.hlw.system.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 岗位展示对象。
 */
@Getter
@Setter
public class PostResp {
    /** 主键编号。 */
    private Long id;
    /** 岗位名称。 */
    private String postName;
    /** 岗位编码。 */
    private String postCode;
    /** 显示顺序。 */
    private Integer orderNum;
    /** 备注。 */
    private String remark;
    /** 状态。 */
    private Integer status;
    /** 是否默认数据（0=系统默认不可删除，1=普通数据可删除）。 */
    private Integer isDefault;
}

package com.hlw.system.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 岗位展示对象。
 */
@Getter
@Setter
public class PostResp {
    /** 表格主键。 */
    private String key;
    /** 岗位名称。 */
    private String postName;
    /** 岗位编码。 */
    private String postCode;
    /** 排序。 */
    private Integer sort;
    /** 状态。 */
    private String status;
    /** 备注。 */
    private String remark;
}

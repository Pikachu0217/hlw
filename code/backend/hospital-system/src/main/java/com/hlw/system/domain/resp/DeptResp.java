package com.hlw.system.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 部门展示对象。
 */
@Getter
@Setter
public class DeptResp {
    /** 表格主键。 */
    private String key;
    /** 主键编号。 */
    private Long id;
    /** 父部门编号。 */
    private Long parentId;
    /** 部门名称。 */
    private String deptName;
    /** 祖级列表。 */
    private String ancestors;
    /** 排序。 */
    private Integer sort;
    /** 状态。 */
    private String status;
}

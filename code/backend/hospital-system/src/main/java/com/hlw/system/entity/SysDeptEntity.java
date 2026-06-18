package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hlw.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统部门持久化对象。
 */
@Getter
@Setter
@TableName("sys_dept")
public class SysDeptEntity extends BaseEntity {
    /** 父部门编号，根部门为 0。 */
    private Long parentId;
    /** 部门名称。 */
    private String deptName;
    /** 祖级列表，逗号分隔。 */
    private String ancestors;
    /** 显示顺序。 */
    private Integer sort;
    /** 部门状态。 */
    private String status;
}

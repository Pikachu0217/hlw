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
    /** 祖级列表，逗号分隔。 */
    private String ancestors;
    /** 部门名称。 */
    private String deptName;
    /** 显示顺序。 */
    private Integer orderNum;
    /** 负责人用户ID。 */
    private String leader;
    /** 联系电话。 */
    private String phone;
    /** 邮箱。 */
    private String email;
    /** 部门状态。 */
    private Integer status;
    /** 是否默认数据（0=系统默认不可删除，1=普通数据可删除）。 */
    private Integer isDefault;
}

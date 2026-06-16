package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hlw.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统岗位持久化对象。
 */
@Getter
@Setter
@TableName("sys_post")
public class SysPostEntity extends BaseEntity {
    /** 岗位名称。 */
    private String postName;
    /** 岗位编码。 */
    private String postCode;
    /** 显示排序。 */
    private Integer sort;
    /** 岗位状态。 */
    private String status;
    /** 备注。 */
    private String remark;
}

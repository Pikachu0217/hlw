package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户岗位关系持久化对象。
 */
@Getter
@Setter
@TableName("sys_user_post")
public class SysUserPostEntity {
    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 租户编号。 */
    private String tenantId;
    /** 用户编号。 */
    private String userId;
    /** 岗位编号。 */
    private Long postId;
}

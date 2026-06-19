package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hlw.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户岗位关系持久化对象。
 */
@Getter
@Setter
@TableName("sys_user_post")
public class SysUserPostEntity extends BaseEntity {
    /** 用户编号。 */
    private String userId;
    /** 岗位编号。 */
    private Long postId;
}

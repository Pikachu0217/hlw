package com.hlw.consult.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 问诊消息持久化对象。
 */
@Getter
@Setter
@TableName("con_message")
public class ConMessageEntity {
    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 租户编号。 */
    private Long tenantId;
    /** 问诊编号。 */
    private Long consultId;
    /** 发送人编号。 */
    private Long senderId;
    /** 发送人类型。 */
    private String senderType;
    /** 消息内容。 */
    private String content;
    /** 消息内容类型。 */
    private String contentType;
    /** 已读标识，非数据库字段，兼容业务布尔语义。 */
    @TableField(exist = false)
    private Boolean readFlag;
    /** 兼容旧表已读标识。 */
    @TableField("is_read")
    private Integer isRead;
    /** 创建时间。 */
    private LocalDateTime createTime;
    /** 更新时间。 */
    private LocalDateTime updateTime;
    /** 创建人编号。 */
    private Long createBy;
    /** 更新人编号。 */
    private Long updateBy;
    /** 逻辑删除标识。 */
    private Integer deleted;
}

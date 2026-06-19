package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hlw.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 通知公告持久化对象。
 */
@Getter
@Setter
@TableName("sys_notice")
public class SysNoticeEntity extends BaseEntity {
    /** 公告标题。 */
    private String noticeTitle;
    /** 公告类型。 */
    private String noticeType;
    /** 公告内容。 */
    private String noticeContent;
    /** 公告状态。 */
    private String status;
    /** 备注。 */
    private String remark;
}

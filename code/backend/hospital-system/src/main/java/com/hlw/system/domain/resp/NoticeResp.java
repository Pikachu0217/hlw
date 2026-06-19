package com.hlw.system.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 通知公告展示对象。
 */
@Getter
@Setter
public class NoticeResp {
    /** 表格主键。 */
    private String key;
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

package com.hlw.system.domain.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建通知公告请求。
 */
@Getter
@Setter
public class CreateNoticeReq {
    /** 公告标题。 */
    @NotBlank(message = "公告标题不能为空")
    private String noticeTitle;
    /** 公告类型。 */
    @NotBlank(message = "公告类型不能为空")
    private String noticeType;
    /** 公告内容。 */
    private String noticeContent;
    /** 公告状态。 */
    private String status;
    /** 备注。 */
    private String remark;
}

package com.hlw.system.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 操作日志展示对象。
 */
@Getter
@Setter
public class OperatorLogResp {
    /** 表格主键。 */
    private String key;
    /** 租户编号。 */
    private String tenantId;
    /** 模块标题。 */
    private String title;
    /** 业务类型。 */
    private Integer businessType;
    /** 方法名称。 */
    private String method;
    /** 请求方式。 */
    private String requestMethod;
    /** 操作类别。 */
    private Integer operatorType;
    /** 操作人员。 */
    private String operatorName;
    /** 部门名称。 */
    private String deptName;
    /** 请求URL。 */
    private String operatorUrl;
    /** 主机地址。 */
    private String operatorIp;
    /** 操作状态。 */
    private Integer status;
    /** 错误消息。 */
    private String errorMsg;
    /** 操作时间。 */
    private String operatorTime;
    /** 消耗时间。 */
    private Long costTime;
}

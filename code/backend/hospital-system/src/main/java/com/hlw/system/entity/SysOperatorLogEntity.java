package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hlw.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 操作日志持久化对象。
 */
@Getter
@Setter
@TableName("sys_operator_log")
public class SysOperatorLogEntity extends BaseEntity {
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
    /** 请求参数。 */
    private String operatorParam;
    /** 返回参数。 */
    private String jsonResult;
    /** 操作状态。 */
    private Integer status;
    /** 错误消息。 */
    private String errorMsg;
    /** 操作时间。 */
    private LocalDateTime operatorTime;
    /** 消耗时间。 */
    private Long costTime;
}

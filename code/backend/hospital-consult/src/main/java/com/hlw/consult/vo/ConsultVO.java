package com.hlw.consult.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 问诊单展示对象。
 */
@Getter
@Setter
public class ConsultVO {    /** 问诊编号。 */
    private Long id;
    /** 问诊单号。 */
    private String consultNo;
    /** 患者姓名。 */
    private String patientName;
    /** 医生姓名。 */
    private String doctorName;
    /** 问诊渠道。 */
    private String channel;
    /** 问诊状态。 */
    private String status;
    /** 前端展示更新时间。 */
    private String updatedAt;
}

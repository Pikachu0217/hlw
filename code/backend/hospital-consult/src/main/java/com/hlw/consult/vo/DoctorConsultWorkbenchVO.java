package com.hlw.consult.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 医生咨询工作台展示对象。
 */
@Getter
@Setter
public class DoctorConsultWorkbenchVO {
    /** 问诊编号。 */
    private Long consultId;
    /** 问诊单号。 */
    private String consultNo;
    /** 患者编号。 */
    private Long patientId;
    /** 患者姓名。 */
    private String patientName;
    /** 医生编号。 */
    private Long doctorId;
    /** 医生姓名。 */
    private String doctorName;
    /** 问诊状态。 */
    private String status;
    /** 问诊渠道。 */
    private String channel;
    /** 前端展示更新时间。 */
    private String updatedAt;
    /** 最新消息内容。 */
    private String lastMessage;
    /** 患者问题描述。 */
    private String chiefComplaint;
    /** 最新消息时间。 */
    private String lastMessageTime;
    /** 剩余问诊秒数。 */
    private Integer remainingSeconds;
}

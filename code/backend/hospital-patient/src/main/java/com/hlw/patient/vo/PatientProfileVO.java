package com.hlw.patient.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 患者档案展示对象。
 */
@Getter
@Setter
public class PatientProfileVO {
    /** 患者编号。 */
    private Long id;
    /** 关联用户编号（关联 sys_user.id）。 */
    private Long userId;
    /** 患者姓名。 */
    private String patientName;
    /** 联系电话。 */
    private String phone;
    /** 脱敏联系电话。 */
    private String maskedPhone;
    /** 患者性别。 */
    private String gender;
    /** 患者年龄。 */
    private Integer age;
    /** 风险等级。 */
    private String riskLevel;
    /** 身份证号。 */
    private String idCard;
    /** 出生日期。 */
    private String birthday;
    /** 联系地址。 */
    private String address;
    /** 最近就诊日期。 */
    private String lastVisit;
    /** 健康档案数量。 */
    private Integer healthRecordCount;
    /** 最新档案摘要。 */
    private String latestRecordSummary;
    /** 最近更新时间。 */
    private String updateTime;
}

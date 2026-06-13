package com.hlw.patient.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 健康档案展示对象。
 */
@Getter
@Setter
public class HealthRecordVO {
    /** 表格主键。 */
    private String key;
    /** 档案编号。 */
    private Long id;
    /** 患者编号。 */
    private Long patientId;
    /** 档案标题。 */
    private String title;
    /** 档案摘要。 */
    private String summary;
    /** 过敏史。 */
    private String allergies;
    /** 既往病史。 */
    private String history;
    /** 诊断信息。 */
    private String diagnosis;
    /** 备注。 */
    private String remark;
    /** 创建时间。 */
    private String createTime;
}

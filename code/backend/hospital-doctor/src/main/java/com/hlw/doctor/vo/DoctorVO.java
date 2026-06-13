package com.hlw.doctor.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 医生展示对象。
 */
@Getter
@Setter
public class DoctorVO {
    /** 表格主键。 */
    private String key;
    /** 医生编号。 */
    private Long id;
    /** 医生姓名。 */
    private String name;
    /** 医生职称。 */
    private String title;
    /** 所属科室。 */
    private String department;
    /** 擅长方向。 */
    private String specialty;
    /** 展示状态。 */
    private String status;
    /** 接诊状态。 */
    private String consultStatus;
    /** 排班描述。 */
    private String schedule;
    /** 当前接诊患者数。 */
    private Integer patientCount;
    /** 问诊费用。 */
    private String consultFee;
}

package com.hlw.appointment.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 预约单展示对象。
 */
@Getter
@Setter
public class AppointmentVO {    /** 预约编号。 */
    private Long id;
    /** 预约单号。 */
    private String appointmentNo;
    /** 患者姓名。 */
    private String patientName;
    /** 医生姓名。 */
    private String doctorName;
    /** 门诊时间。 */
    private String clinicTime;
    /** 预约来源。 */
    private String source;
    /** 预约状态。 */
    private String status;
    /** 预约费用。 */
    private String feeAmount;
}

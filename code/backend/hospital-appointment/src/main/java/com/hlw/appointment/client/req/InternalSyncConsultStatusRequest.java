package com.hlw.appointment.client.req;

import lombok.Getter;
import lombok.Setter;

/**
 * 内部同步问诊状态请求。
 */
@Getter
@Setter
public class InternalSyncConsultStatusRequest {
    /** 预约单编号。 */
    private Long appointmentId;
    /** 问诊状态，为空表示不修改。 */
    private String status;
    /** 支付状态，为空表示不修改。 */
    private String payStatus;
    /** 状态变更原因。 */
    private String reason;
}

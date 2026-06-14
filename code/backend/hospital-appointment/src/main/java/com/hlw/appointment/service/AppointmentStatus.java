package com.hlw.appointment.service;

/**
 * 预约状态枚举。
 */
public enum AppointmentStatus {
    PENDING_PAY("待支付"),
    PAID("已支付"),
    CHECKED_IN("已签到"),
    COMPLETED("已完成"),
    CANCELLED("已取消"),
    GRABBED("已接单");

    private final String dbValue;

    AppointmentStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String dbValue() {
        return dbValue;
    }

    public static AppointmentStatus fromDbValue(String dbValue) {
        for (AppointmentStatus status : values()) {
            if (status.dbValue.equals(dbValue)) {
                return status;
            }
        }
        return PENDING_PAY;
    }
}

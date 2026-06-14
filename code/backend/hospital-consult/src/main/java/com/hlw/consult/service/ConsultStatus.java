package com.hlw.consult.service;

/**
 * 问诊状态枚举。
 */
public enum ConsultStatus {
    WAITING("待接单"),
    IN_PROGRESS("咨询中"),
    COMPLETED("已完成"),
    TIMEOUT("已超时");

    private final String dbValue;

    ConsultStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String dbValue() {
        return dbValue;
    }

    /**
     * 根据数据库存储值反查枚举。
     *
     * @param dbValue 数据库存储值
     * @return 对应枚举，无法匹配时返回 IN_PROGRESS
     */
    public static ConsultStatus fromDbValue(String dbValue) {
        for (ConsultStatus status : values()) {
            if (status.dbValue.equals(dbValue)) {
                return status;
            }
        }
        return IN_PROGRESS;
    }
}

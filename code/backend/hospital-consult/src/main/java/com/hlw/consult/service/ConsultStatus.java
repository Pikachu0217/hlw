package com.hlw.consult.service;

/**
 * 问诊状态枚举。
 */
public enum ConsultStatus {
    /** 待接单。 */
    WAITING("0", "待接单"),
    /** 咨询中。 */
    IN_PROGRESS("1", "咨询中"),
    /** 已延长。 */
    EXTENDED("2", "已延长"),
    /** 已完成。 */
    FINISHED("3", "已完成"),
    /** 已取消。 */
    CANCELLED("4", "已取消"),
    /** 已超时。 */
    TIMEOUT("5", "已超时"),
    /** 已拒诊。 */
    REJECTED("6", "已拒诊");

    /** 数据库存储值。 */
    private final String dbValue;
    /** 前端展示文案。 */
    private final String label;

    ConsultStatus(String dbValue, String label) {
        this.dbValue = dbValue;
        this.label = label;
    }

    /**
     * 获取数据库存储值。
     *
     * @return 数据库存储值
     */
    public String dbValue() {
        return dbValue;
    }

    /**
     * 获取展示文案。
     *
     * @return 展示文案
     */
    public String label() {
        return label;
    }

    /**
     * 根据数据库存储值反查枚举。
     *
     * @param dbValue 数据库存储值
     * @return 对应枚举，无法匹配时返回 WAITING
     */
    public static ConsultStatus fromDbValue(String dbValue) {
        for (ConsultStatus status : values()) {
            if (status.dbValue.equals(dbValue) || status.label.equals(dbValue)) {
                return status;
            }
        }
        return WAITING;
    }
}

package com.hlw.consult.service;

import java.util.Set;

/**
 * 问诊展示状态常量。
 */
public final class ConsultDisplayStatus {
    /** 待接单状态码。 */
    public static final String WAITING = ConsultStatus.WAITING.dbValue();
    /** 咨询中状态码。 */
    public static final String IN_PROGRESS = ConsultStatus.IN_PROGRESS.dbValue();
    /** 已延长状态码。 */
    public static final String EXTENDED = ConsultStatus.EXTENDED.dbValue();
    /** 已完成状态码。 */
    public static final String FINISHED = ConsultStatus.FINISHED.dbValue();
    /** 已取消状态码。 */
    public static final String CANCELLED = ConsultStatus.CANCELLED.dbValue();
    /** 已超时状态码。 */
    public static final String TIMEOUT = ConsultStatus.TIMEOUT.dbValue();
    /** 已拒诊状态码。 */
    public static final String REJECTED = ConsultStatus.REJECTED.dbValue();
    /** 医生工作台展示状态集合。 */
    public static final Set<String> DOCTOR_WORKBENCH_STATUSES = Set.of(WAITING, IN_PROGRESS, EXTENDED, FINISHED);

    /**
     * 私有构造方法，防止实例化常量类。
     */
    private ConsultDisplayStatus() {
    }

    /**
     * 将数据库状态码转换为展示文案。
     *
     * @param status 数据库存储状态
     * @return 展示文案
     */
    public static String labelOf(String status) {
        return ConsultStatus.fromDbValue(status).label();
    }
}

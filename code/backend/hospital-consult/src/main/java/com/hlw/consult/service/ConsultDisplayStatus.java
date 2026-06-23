package com.hlw.consult.service;

import java.util.Set;

/**
 * 问诊展示状态常量。
 */
public final class ConsultDisplayStatus {
    /** 待接单。 */
    public static final String WAITING = "待接单";
    /** 咨询中。 */
    public static final String IN_PROGRESS = "咨询中";
    /** 已延长。 */
    public static final String EXTENDED = "已延长";
    /** 已完成。 */
    public static final String FINISHED = "已完成";
    /** 已取消。 */
    public static final String CANCELLED = "已取消";
    /** 已超时。 */
    public static final String TIMEOUT = "已超时";
    /** 医生工作台展示状态集合。 */
    public static final Set<String> DOCTOR_WORKBENCH_STATUSES = Set.of(WAITING, IN_PROGRESS, EXTENDED);

    /**
     * 私有构造方法，防止实例化常量类。
     */
    private ConsultDisplayStatus() {
    }
}

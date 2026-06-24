package com.hlw.appointment.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 号源统计信息响应对象。
 * <p>用于前端号源池的"可用/锁定/已用"统计展示，数据从放号配置容量和号源表状态实时汇总。</p>
 */
@Getter
@Setter
public class NumberSourceStatsVO {
    /** 排班编号。 */
    private Long scheduleId;
    /** 总容量（放号配置 release_count 汇总）。 */
    private long totalCapacity;
    /** 已锁定的号源数。 */
    private long lockedCount;
    /** 已使用的号源数。 */
    private long usedCount;
    /** 可锁号源数（totalCapacity - lockedCount - usedCount）。 */
    private long availableCount;
}

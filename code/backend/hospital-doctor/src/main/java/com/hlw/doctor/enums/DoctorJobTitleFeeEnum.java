package com.hlw.doctor.enums;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * 医生职称费用枚举。
 */
public enum DoctorJobTitleFeeEnum {
    /** 主任医师费用。 */
    CHIEF_PHYSICIAN("主任医师", new BigDecimal("50.00")),
    /** 副主任医师费用。 */
    ASSOCIATE_CHIEF_PHYSICIAN("副主任医师", new BigDecimal("20.00")),
    /** 主治医师费用。 */
    ATTENDING_PHYSICIAN("主治医师", new BigDecimal("10.00"));

    /** 医生职称。 */
    private final String title;
    /** 问诊费用。 */
    private final BigDecimal fee;

    /**
     * 构造医生职称费用枚举。
     *
     * @param title 医生职称
     * @param fee 问诊费用
     */
    DoctorJobTitleFeeEnum(String title, BigDecimal fee) {
        this.title = title;
        this.fee = fee;
    }

    /**
     * 获取医生职称。
     *
     * @return 医生职称
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取问诊费用。
     *
     * @return 问诊费用
     */
    public BigDecimal getFee() {
        return fee;
    }

    /**
     * 根据医生职称解析问诊费用。
     *
     * @param title 医生职称
     * @return 问诊费用
     */
    public static BigDecimal resolveFee(String title) {
        return Arrays.stream(values())
            .filter(item -> item.title.equals(title))
            .map(DoctorJobTitleFeeEnum::getFee)
            .findFirst()
            .orElse(BigDecimal.ZERO);
    }
}

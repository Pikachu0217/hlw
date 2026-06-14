package com.hlw.common.core.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultValueUtilsTest {
    @Test
    void defaultIfBlank_returns_value_when_not_blank() {
        assertThat(DefaultValueUtils.defaultIfBlank("hello", "default")).isEqualTo("hello");
    }

    @Test
    void defaultIfBlank_returns_default_when_null() {
        assertThat(DefaultValueUtils.defaultIfBlank(null, "default")).isEqualTo("default");
    }

    @Test
    void defaultIfBlank_returns_default_when_blank() {
        assertThat(DefaultValueUtils.defaultIfBlank("   ", "default")).isEqualTo("default");
    }

    @Test
    void defaultIfBlank_trims_value() {
        assertThat(DefaultValueUtils.defaultIfBlank("  hello  ", "default")).isEqualTo("hello");
    }

    @Test
    void defaultIfNull_returns_value_when_not_null() {
        assertThat(DefaultValueUtils.defaultIfNull(42L, 0L)).isEqualTo(42L);
        assertThat(DefaultValueUtils.defaultIfNull(5, 0)).isEqualTo(5);
        assertThat(DefaultValueUtils.defaultIfNull(new BigDecimal("10"), BigDecimal.ZERO))
                .isEqualTo(new BigDecimal("10"));
    }

    @Test
    void defaultIfNull_returns_default_when_null() {
        assertThat(DefaultValueUtils.defaultIfNull((Long) null, 0L)).isEqualTo(0L);
        assertThat(DefaultValueUtils.defaultIfNull((Integer) null, 0)).isEqualTo(0);
        assertThat(DefaultValueUtils.defaultIfNull((BigDecimal) null, BigDecimal.ZERO))
                .isEqualTo(BigDecimal.ZERO);
    }
}

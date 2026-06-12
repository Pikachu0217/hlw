package com.hlw.common.core.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RTest {
    @Test
    void ok_wraps_success_payload() {
        R<String> result = R.ok("pong");

        assertThat(result.code()).isEqualTo(200);
        assertThat(result.message()).isEqualTo("success");
        assertThat(result.data()).isEqualTo("pong");
    }

    @Test
    void fail_wraps_error_without_payload() {
        R<String> result = R.fail(401, "未登录");

        assertThat(result.code()).isEqualTo(401);
        assertThat(result.message()).isEqualTo("未登录");
        assertThat(result.data()).isNull();
    }
}

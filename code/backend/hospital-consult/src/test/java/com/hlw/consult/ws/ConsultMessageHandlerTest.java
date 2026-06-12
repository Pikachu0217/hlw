package com.hlw.consult.ws;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsultMessageHandlerTest {
    @Test
    void text_message_is_saved_and_broadcast_as_json() {
        ConsultMessageHandler handler = new ConsultMessageHandler(new InMemoryConsultMessageRepository());

        String json = handler.handle(1L, 2L, "{\"type\":\"CHAT\",\"content\":\"你好\",\"contentType\":\"TEXT\"}");

        assertThat(json).contains("\"type\":\"CHAT\"");
        assertThat(json).contains("\"content\":\"你好\"");
        assertThat(json).contains("\"contentType\":\"TEXT\"");
    }
}

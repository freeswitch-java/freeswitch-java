package com.freeswitchjava.esl.command;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SendMsgTest {

    @Test
    void execute_event_uuid_uses_event_uuid_header() {
        SendMsg msg = SendMsg.execute("playback")
                .arg("/tmp/demo.wav")
                .eventUuid("app-123")
                .build();

        assertThat(msg.toFrame()).contains("Event-UUID: app-123");
        assertThat(msg.toFrame()).doesNotContain("event-lock-uuid: app-123");
    }

    @Test
    void execute_supports_event_lock_headers() {
        SendMsg msg = SendMsg.execute("bridge")
                .arg("sofia/internal/1000")
                .async(true)
                .eventLock(true)
                .eventLockUuid("lock-123")
                .build();

        assertThat(msg.toFrame()).contains("async: true");
        assertThat(msg.toFrame()).contains("event-lock: true");
        assertThat(msg.toFrame()).contains("event-lock-uuid: lock-123");
    }
}

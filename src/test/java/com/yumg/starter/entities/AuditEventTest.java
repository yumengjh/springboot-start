package com.yumg.starter.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AuditEventTest {

    @Test
    void capturesActorResultAndSanitizedMetadata() {
        AuditEvent event = new AuditEvent("user-1", "LOGIN_FAILED", "User", "user-2",
                "FAILURE", "trace-1", "{\"reason\":\"invalid_credentials\"}");

        assertThat(event.getActorId()).isEqualTo("user-1");
        assertThat(event.getResult()).isEqualTo("FAILURE");
        assertThat(event.getMetadata()).doesNotContain("password").doesNotContain("token");
    }
}

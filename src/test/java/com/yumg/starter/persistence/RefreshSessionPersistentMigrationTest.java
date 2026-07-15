package com.yumg.starter.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class RefreshSessionPersistentMigrationTest {

    @Test
    void migrationNormalizesLegacyNullPersistentValues() throws Exception {
        String changelog = new String(new ClassPathResource(
                "db/changelog/changes/009-normalize-refresh-session-persistence.yaml")
                .getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        assertThat(changelog).contains("update refresh_sessions set persistent = false where persistent is null");
    }
}

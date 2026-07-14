package com.yumg.starter.modules.security.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.Properties;
import org.springframework.boot.info.GitProperties;

class SystemInfoServiceTest {
    @Test
    void returnsNullInsteadOfInventingUnavailableBuildMetadata() {
        var response = new SystemInfoService(Optional.empty(), Optional.empty()).info();

        assertThat(response.version()).isNull();
        assertThat(response.gitCommit()).isNull();
        assertThat(response.buildTime()).isNull();
    }

    @Test
    void exposesCommitWhenBuildContainsSpringBootCompatibleProperty() {
        Properties properties = new Properties();
        // Spring Boot strips the `git.` resource prefix before creating GitProperties.
        properties.setProperty("commit.id", "ef8a3be0123456789");

        var response = new SystemInfoService(Optional.empty(), Optional.of(new GitProperties(properties))).info();

        assertThat(response.gitCommit()).isEqualTo("ef8a3be0123456789");
    }
}

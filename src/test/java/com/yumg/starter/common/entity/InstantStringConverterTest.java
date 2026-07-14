package com.yumg.starter.common.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class InstantStringConverterTest {

    private final InstantStringConverter converter = new InstantStringConverter();

    @Test
    void readsLegacySqliteTimestampFormats() {
        assertThat(converter.convertToEntityAttribute("2026-07-14 10:00:00.123456"))
                .isEqualTo(Instant.parse("2026-07-14T10:00:00.123456Z"));
        assertThat(converter.convertToEntityAttribute("1784025350613"))
                .isEqualTo(Instant.ofEpochMilli(1784025350613L));
    }
}

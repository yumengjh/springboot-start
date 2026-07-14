package com.yumg.starter.common.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.Instant;

@Converter
public class InstantStringConverter implements AttributeConverter<Instant, String> {
    @Override
    public String convertToDatabaseColumn(Instant attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public Instant convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        if (value.chars().allMatch(Character::isDigit)) {
            return Instant.ofEpochMilli(Long.parseLong(value));
        }
        if (value.indexOf('T') < 0) {
            return Instant.parse(value.replace(' ', 'T') + "Z");
        }
        return Instant.parse(value);
    }
}

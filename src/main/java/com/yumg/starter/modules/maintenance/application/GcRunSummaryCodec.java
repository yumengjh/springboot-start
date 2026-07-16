package com.yumg.starter.modules.maintenance.application;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class GcRunSummaryCodec {
    private final ObjectMapper objectMapper;

    public GcRunSummaryCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String write(GcRunSummary summary) {
        try {
            return objectMapper.writeValueAsString(summary);
        } catch (JacksonException exception) {
            throw new IllegalStateException("无法序列化 GC 运行结果", exception);
        }
    }

    public GcRunSummary read(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(value, GcRunSummary.class);
        } catch (JacksonException exception) {
            return null;
        }
    }
}

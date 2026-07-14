package com.yumg.starter.common.api;

import java.util.List;

public record ApiError(
    String code,
    String message,
    String traceId,
    List<FieldViolation> violations,
    long timestamp
) {
    public ApiError {
        violations = violations == null ? List.of() : List.copyOf(violations);
    }
}

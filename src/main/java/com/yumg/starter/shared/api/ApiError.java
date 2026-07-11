package com.yumg.starter.shared.api;

import java.time.Instant;
import java.util.List;

public record ApiError(
    String code,
    String message,
    String traceId,
    List<FieldViolation> violations,
    Instant timestamp
) {
    public ApiError {
        violations = violations == null ? List.of() : List.copyOf(violations);
    }
}

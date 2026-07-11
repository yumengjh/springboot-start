package com.yumg.starter.common.api;

import com.yumg.starter.common.web.TraceIdFilter;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;

final class ApiErrorFactory {
    private ApiErrorFactory() {}

    static ApiError create(ApiErrorCode error, List<FieldViolation> violations) {
        return new ApiError(error.name(), error.message(), traceId(), violations, Instant.now());
    }

    static ResponseEntity<ApiError> response(ApiErrorCode error, List<FieldViolation> violations) {
        ApiError body = create(error, violations);
        return ResponseEntity.status(error.status())
            .header(TraceIdFilter.TRACE_ID_HEADER, body.traceId())
            .body(body);
    }

    private static String traceId() {
        String traceId = MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY);
        return traceId == null || traceId.isBlank()
            ? UUID.randomUUID().toString().replace("-", "") : traceId;
    }
}

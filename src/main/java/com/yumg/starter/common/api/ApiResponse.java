package com.yumg.starter.common.api;

import java.time.Instant;

public record ApiResponse<T>(T data, String traceId, Instant timestamp) {
}

package com.yumg.starter.common.api;

public record ApiResponse<T>(T data, String traceId, long timestamp) {
}

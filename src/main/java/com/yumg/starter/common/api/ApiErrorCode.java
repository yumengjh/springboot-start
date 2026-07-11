package com.yumg.starter.common.api;

import org.springframework.http.HttpStatus;

enum ApiErrorCode {
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Request validation failed"),
    MALFORMED_JSON(HttpStatus.BAD_REQUEST, "Malformed JSON request"),
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "Authentication is required"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access is denied"),
    OPTIMISTIC_LOCK_CONFLICT(HttpStatus.CONFLICT, "The resource was modified by another request"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    CONFLICT(HttpStatus.CONFLICT, "The request conflicts with existing data"),
    RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "Too many requests"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");

    private final HttpStatus status;
    private final String message;

    ApiErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    HttpStatus status() { return status; }
    String message() { return message; }
}

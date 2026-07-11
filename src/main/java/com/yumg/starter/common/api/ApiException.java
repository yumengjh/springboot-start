package com.yumg.starter.common.api;

public class ApiException extends RuntimeException {
    private final ApiErrorCode error;

    private ApiException(ApiErrorCode error) {
        super(error.message());
        this.error = error;
    }

    public static ApiException notFound() {
        return new ApiException(ApiErrorCode.NOT_FOUND);
    }

    public static ApiException conflict() {
        return new ApiException(ApiErrorCode.CONFLICT);
    }

    public static ApiException rateLimited() {
        return new ApiException(ApiErrorCode.RATE_LIMITED);
    }

    ApiErrorCode error() { return error; }
}

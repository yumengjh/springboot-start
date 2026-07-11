package com.yumg.starter.common.api;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException exception) {
        List<FieldViolation> violations = exception.getBindingResult().getFieldErrors().stream()
            .map(error -> new FieldViolation(error.getField(), safeMessage(error.getDefaultMessage())))
            .toList();
        return ApiErrorFactory.response(ApiErrorCode.VALIDATION_FAILED, violations);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ApiError> methodValidation(HandlerMethodValidationException exception) {
        List<FieldViolation> violations = exception.getParameterValidationResults().stream()
            .flatMap(result -> result.getResolvableErrors().stream()
                .map(error -> new FieldViolation(parameterName(result), safeMessage(error.getDefaultMessage()))))
            .toList();
        return ApiErrorFactory.response(ApiErrorCode.VALIDATION_FAILED, violations);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> malformedJson() {
        return ApiErrorFactory.response(ApiErrorCode.MALFORMED_JSON, List.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiError> authentication(AuthenticationException ignored) {
        return authentication();
    }

    ResponseEntity<ApiError> authentication() {
        return ApiErrorFactory.response(ApiErrorCode.AUTHENTICATION_REQUIRED, List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> authorization(AccessDeniedException ignored) {
        return authorization();
    }

    ResponseEntity<ApiError> authorization() {
        return ApiErrorFactory.response(ApiErrorCode.ACCESS_DENIED, List.of());
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, OptimisticLockException.class})
    ResponseEntity<ApiError> optimisticLock(Exception ignored) { return optimisticLock(); }
    ResponseEntity<ApiError> optimisticLock() {
        return ApiErrorFactory.response(ApiErrorCode.OPTIMISTIC_LOCK_CONFLICT, List.of());
    }

    @ExceptionHandler({EntityNotFoundException.class, NoResourceFoundException.class})
    ResponseEntity<ApiError> notFound(Exception ignored) { return notFound(); }
    ResponseEntity<ApiError> notFound() {
        return ApiErrorFactory.response(ApiErrorCode.NOT_FOUND, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> conflict(DataIntegrityViolationException ignored) { return conflict(); }
    ResponseEntity<ApiError> conflict() {
        return ApiErrorFactory.response(ApiErrorCode.CONFLICT, List.of());
    }

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiError> apiException(ApiException exception) {
        return ApiErrorFactory.response(exception.error(), List.of());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ApiError> unsupportedMediaType() {
        return ApiErrorFactory.response(ApiErrorCode.UNSUPPORTED_MEDIA_TYPE, List.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception ignored) { return unexpected(); }
    ResponseEntity<ApiError> unexpected() {
        return ApiErrorFactory.response(ApiErrorCode.INTERNAL_ERROR, List.of());
    }

    private String parameterName(org.springframework.validation.method.ParameterValidationResult result) {
        String name = result.getMethodParameter().getParameterName();
        return name == null ? "request" : name;
    }

    private String safeMessage(String message) {
        return message == null || message.isBlank() ? "Invalid value" : message;
    }
}

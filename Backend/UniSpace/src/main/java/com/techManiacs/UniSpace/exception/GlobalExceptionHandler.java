package com.techManiacs.UniSpace.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException exception, HttpServletRequest request) {
        return response(exception.getStatus(), exception.getMessage(), request);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiError> handleBadRequest(Exception exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "Invalid request", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleIntegrityViolation(
            DataIntegrityViolationException exception, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "The requested change conflicts with existing data", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(Exception exception, HttpServletRequest request) {
        log.error("Unhandled exception for {} {}", request.getMethod(), request.getRequestURI(), exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String message, HttpServletRequest request) {
        ApiError error = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI());
        return ResponseEntity.status(status).body(error);
    }
}

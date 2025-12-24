package org.collegemanagement.exception;


import org.collegemanagement.api.response.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFoundException(
            ResourceNotFoundException ex) {

        ApiResponse<?> response = ApiResponse.error(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                List.of(ApiResponse.ErrorDetail.builder()
                        .code("RESOURCE_NOT_FOUND")
                        .message(ex.getMessage())
                        .build())
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }


    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthenticationException(
            AuthenticationException ex) {

        ApiResponse<?> response = ApiResponse.error(
                HttpStatus.FORBIDDEN.value(),
                "Access denied",
                List.of(ApiResponse.ErrorDetail.builder()
                        .code("ACCESS_DENIED")
                        .message(ex.getMessage())
                        .build())
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }


    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ApiResponse<?>> handleConflictException(
            ResourceConflictException ex) {

        ApiResponse<?> response = ApiResponse.error(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                List.of(ApiResponse.ErrorDetail.builder()
                        .code("RESOURCE_CONFLICT")
                        .message(ex.getMessage())
                        .build())
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }


    @ExceptionHandler(InvalidUserNameOrPasswordException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidCredentialsException(
            InvalidUserNameOrPasswordException ex) {

        ApiResponse<?> response = ApiResponse.error(
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage(),
                List.of(ApiResponse.ErrorDetail.builder()
                        .code("INVALID_CREDENTIALS")
                        .message(ex.getMessage())
                        .build())
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }


    /**
     * 1️⃣ DTO Validation Errors (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(
            MethodArgumentNotValidException ex) {

        List<ApiResponse.ErrorDetail> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> ApiResponse.ErrorDetail.builder()
                        .field(err.getField())
                        .code("VALIDATION_ERROR")
                        .message(err.getDefaultMessage())
                        .build())
                .toList();

        ApiResponse<?> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                errors
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 2️⃣ Database Constraint Violations
     * (NOT NULL, UNIQUE, FK, etc.)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {

        String rootMessage = ex.getMostSpecificCause().getMessage();

        ApiResponse.ErrorDetail error;

        if (rootMessage != null && rootMessage.contains("country")) {
            error = ApiResponse.ErrorDetail.builder()
                    .field("country")
                    .code("NOT_NULL")
                    .message("Country is required and cannot be empty")
                    .build();
        } else if (rootMessage != null && rootMessage.contains("Duplicate entry")) {
            error = ApiResponse.ErrorDetail.builder()
                    .code("DUPLICATE_VALUE")
                    .message("Duplicate value already exists")
                    .build();
        } else {
            error = ApiResponse.ErrorDetail.builder()
                    .code("DATA_INTEGRITY_VIOLATION")
                    .message("Invalid data provided")
                    .build();
        }

        ApiResponse<?> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid request data",
                List.of(error)
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 3️⃣ Generic / Fallback Exception
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(Exception ex) {

        ApiResponse.ErrorDetail error = ApiResponse.ErrorDetail.builder()
                .code("INTERNAL_SERVER_ERROR")
                .message("Something went wrong. Please try again later.")
                .build();

        ApiResponse<?> response = ApiResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error",
                List.of(error)
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}

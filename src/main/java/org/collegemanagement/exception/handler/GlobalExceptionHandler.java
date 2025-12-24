package org.collegemanagement.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.api.response.ApiResponse;
import org.collegemanagement.exception.base.BusinessException;
import org.collegemanagement.exception.code.ErrorCode;
import org.collegemanagement.exception.factory.ApiErrorResponseFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusiness(BusinessException ex) {

        String requestId = UUID.randomUUID().toString();
        log.warn("[{}] Business error: {}", requestId, ex.getMessage());

        ApiResponse<?> response =
                ApiErrorResponseFactory.from(ex.getErrorCode(), ex.getMessage());

        response.getMetadata().setRequestId(requestId);

        return ResponseEntity
                .status(ex.getErrorCode().getStatus())
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException ex) {

        var errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(err -> ApiResponse.ErrorDetail.builder()
                        .field(err.getField())
                        .code(ErrorCode.VALIDATION_ERROR.name())
                        .message(err.getDefaultMessage())
                        .build())
                .toList();

        ApiResponse<?> response = ApiResponse.error(
                ErrorCode.VALIDATION_ERROR.getStatus().value(),
                ErrorCode.VALIDATION_ERROR.getDefaultMessage(),
                errors
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDbViolation(DataIntegrityViolationException ex) {

        log.error("Database constraint violation", ex);

        ApiResponse<?> response =
                ApiErrorResponseFactory.from(ErrorCode.DATA_INTEGRITY_VIOLATION);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleUnexpected(Exception ex) {

        log.error("Unexpected error", ex);

        ApiResponse<?> response =
                ApiErrorResponseFactory.from(ErrorCode.INTERNAL_SERVER_ERROR);

        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(response);
    }
}

package org.collegemanagement.exception.code;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode implements ErrorCodeContract {

    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid username or password"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access denied"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    RESOURCE_CONFLICT(HttpStatus.CONFLICT, "Resource conflict"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Validation failed"),
    DATA_INTEGRITY_VIOLATION(HttpStatus.BAD_REQUEST, "Invalid request data"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    TENANT_REQUIRED(HttpStatus.UNAUTHORIZED, "Tenant required id to access this resource"),;

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }
}


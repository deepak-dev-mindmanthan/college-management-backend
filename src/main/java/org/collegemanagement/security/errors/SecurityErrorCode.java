package org.collegemanagement.security.errors;

import lombok.Getter;
import org.collegemanagement.exception.code.ErrorCodeContract;
import org.springframework.http.HttpStatus;

@Getter
public enum SecurityErrorCode implements ErrorCodeContract {

    UN_AUTHENTICATED(HttpStatus.UNAUTHORIZED, "User not authenticated"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access denied"),
    CROSS_COLLEGE_ACCESS(HttpStatus.FORBIDDEN,
            "You are not allowed to access or modify data belonging to another college."),
    INSUFFICIENT_ROLE(HttpStatus.FORBIDDEN, "Insufficient role"),
    SUBSCRIPTION_EXPIRED(HttpStatus.FORBIDDEN, "Subscription expired"),
    TENANT_CONTEXT_MISSING(HttpStatus.BAD_REQUEST, "Tenant context missing"),
    INVALID_TENANT(HttpStatus.BAD_REQUEST, "Invalid tenant"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid credentials"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    RESOURCE_CONFLICT(HttpStatus.CONFLICT, "Resource conflict"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid or malformed access token");

    private final HttpStatus status;
    private final String defaultMessage;

    SecurityErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }
}

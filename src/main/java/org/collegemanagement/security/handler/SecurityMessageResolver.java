package org.collegemanagement.security.handler;

import org.collegemanagement.security.errors.SecurityErrorCode;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class SecurityMessageResolver {

    public String resolve(String errorCode) {
        if (errorCode == null) {
            return defaultMessage();
        }

        try {
            SecurityErrorCode code = SecurityErrorCode.valueOf(errorCode);
            return switch (code) {
                case CROSS_COLLEGE_ACCESS ->
                        "You are not allowed to access or modify data belonging to another college.";

                case SUBSCRIPTION_EXPIRED -> "Your subscription has expired. Please renew to continue.";

                case INSUFFICIENT_ROLE -> "You do not have permission to perform this action.";

                case UN_AUTHENTICATED -> "User not authenticated.";

                case TENANT_CONTEXT_MISSING -> "Tenant context missing.";

                case INVALID_TENANT -> "Invalid tenant.";

                case ACCESS_DENIED -> defaultMessage();

            };
        } catch (IllegalArgumentException ex) {
            return defaultMessage();
        }
    }

    private String defaultMessage() {
        return "You are not allowed to perform this action.";
    }
}

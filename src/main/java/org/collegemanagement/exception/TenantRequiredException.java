package org.collegemanagement.exception;

import org.collegemanagement.exception.base.BusinessException;
import org.collegemanagement.exception.code.ErrorCode;

/**
 * Thrown when a tenant-scoped operation is executed
 * without an active tenant context.
 *
 * Typically occurs for SUPER_ADMIN when tenant is not explicitly selected.
 */
public class TenantRequiredException extends BusinessException {

    public TenantRequiredException(String message) {
        super(ErrorCode.TENANT_REQUIRED, message);
    }
}

package org.collegemanagement.exception;


import org.collegemanagement.exception.base.BusinessException;
import org.collegemanagement.exception.code.ErrorCode;

public class ResourceConflictException extends BusinessException {
    public ResourceConflictException(String message) {
        super(ErrorCode.RESOURCE_CONFLICT, message);
    }
}

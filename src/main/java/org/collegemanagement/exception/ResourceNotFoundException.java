package org.collegemanagement.exception;


import org.collegemanagement.exception.base.BusinessException;
import org.collegemanagement.exception.code.ErrorCode;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }
}


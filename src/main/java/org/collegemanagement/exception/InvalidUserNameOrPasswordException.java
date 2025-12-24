package org.collegemanagement.exception;

import org.collegemanagement.exception.base.BusinessException;
import org.collegemanagement.exception.code.ErrorCode;

public class InvalidUserNameOrPasswordException extends BusinessException {
    public InvalidUserNameOrPasswordException() {
        super(ErrorCode.INVALID_CREDENTIALS);
    }
}

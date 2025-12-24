package org.collegemanagement.exception.base;


import lombok.Getter;
import org.collegemanagement.exception.code.ErrorCodeContract;

@Getter
public abstract class BusinessException extends RuntimeException {

    private final ErrorCodeContract errorCode;

    protected BusinessException(ErrorCodeContract errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    protected BusinessException(ErrorCodeContract errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}

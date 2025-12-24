package org.collegemanagement.exception.code;

import org.springframework.http.HttpStatus;

public interface ErrorCodeContract {
    HttpStatus getStatus();
    String getDefaultMessage();
    String name();
}


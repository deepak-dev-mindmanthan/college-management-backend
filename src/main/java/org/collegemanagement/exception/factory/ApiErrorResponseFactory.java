package org.collegemanagement.exception.factory;


import org.collegemanagement.api.response.ApiResponse;
import org.collegemanagement.exception.code.ErrorCodeContract;

import java.util.List;

public final class ApiErrorResponseFactory {

    private ApiErrorResponseFactory() {}

    public static ApiResponse<?> from(ErrorCodeContract code) {
        return ApiResponse.error(
                code.getStatus().value(),
                code.getDefaultMessage(),
                List.of(ApiResponse.ErrorDetail.builder()
                        .code(code.name())
                        .message(code.getDefaultMessage())
                        .build())
        );
    }

    public static ApiResponse<?> from(ErrorCodeContract code, String message) {
        return ApiResponse.error(
                code.getStatus().value(),
                message,
                List.of(ApiResponse.ErrorDetail.builder()
                        .code(code.name())
                        .message(message)
                        .build())
        );
    }
}

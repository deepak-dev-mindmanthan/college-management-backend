package org.collegemanagement.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private int status;
    private String message;
    private T data;
    private List<ErrorDetail> errors;
    private ResponseMetadata metadata;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetail {
        private String field;
        private String code;
        private String message;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResponseMetadata {

        @Builder.Default
        private LocalDateTime timestamp = LocalDateTime.now();

        private String requestId;

        @Builder.Default
        private String version = "v1";

        private Integer page;
        private Integer size;
        private Long totalElements;
    }

    // ---- Factory Methods ----

    public static <T> ApiResponse<T> success(T data, String message, int status) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(status)
                .message(message)
                .data(data)
                .metadata(ResponseMetadata.builder().build())
                .build();
    }

    public static <T> ApiResponse<T> error(int status, String message, List<ErrorDetail> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .status(status)
                .message(message)
                .errors(errors)
                .metadata(ResponseMetadata.builder().build())
                .build();
    }
}


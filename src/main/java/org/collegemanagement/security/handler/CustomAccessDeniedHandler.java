package org.collegemanagement.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.collegemanagement.exception.factory.ApiErrorResponseFactory;
import org.collegemanagement.security.errors.SecurityErrorCode;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(@Nullable HttpServletRequest request,
                       HttpServletResponse response,
                       @Nullable AccessDeniedException ex) throws IOException {

        if (response.isCommitted()) {
            return;
        }

        SecurityErrorCode code = resolve(ex);

        var apiResponse =
                ApiErrorResponseFactory.from(code);

        response.setStatus(code.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }

    private SecurityErrorCode resolve(AccessDeniedException ex) {
        try {
            return SecurityErrorCode.valueOf(ex.getMessage());
        } catch (Exception ignored) {
            return SecurityErrorCode.ACCESS_DENIED;
        }
    }
}

package org.collegemanagement.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.collegemanagement.exception.factory.ApiErrorResponseFactory;
import org.collegemanagement.security.errors.SecurityErrorCode;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(@NonNull HttpServletRequest request, HttpServletResponse response, @NonNull AuthenticationException ex) throws IOException {

        if (response.isCommitted()) {
            return;
        }

        var apiResponse = ApiErrorResponseFactory.from(resolveErrorCode(ex));

        response.setStatus(SecurityErrorCode.UN_AUTHENTICATED.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }

    private SecurityErrorCode resolveErrorCode(AuthenticationException ex) {
        if (ex instanceof InvalidBearerTokenException) {
            return SecurityErrorCode.INVALID_TOKEN;
        }
        return SecurityErrorCode.UN_AUTHENTICATED;
    }
}

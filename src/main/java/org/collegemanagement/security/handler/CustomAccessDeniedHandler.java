package org.collegemanagement.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.collegemanagement.security.errors.SecurityErrorCode;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {


    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SecurityMessageResolver messageResolver;

    public CustomAccessDeniedHandler(SecurityMessageResolver messageResolver) {
        this.messageResolver = messageResolver;
    }

    @Override
    public void handle(@Nullable HttpServletRequest request,
                       HttpServletResponse response,
                       @Nullable AccessDeniedException ex) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String errorCode =
                (ex != null && ex.getMessage() != null)
                        ? ex.getMessage()
                        : SecurityErrorCode.ACCESS_DENIED.name();


        String message = messageResolver.resolve(errorCode);

        Map<String, Object> body = Map.of(
                "success", false,
                "message", message
        );

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}


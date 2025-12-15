package org.collegemanagement.security.handler;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(@Nullable HttpServletRequest request,
                         HttpServletResponse response,
                         @Nullable  AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        response.getWriter().write(String.format("""
            {
              "status": 401,
              "error": "Unauthorized",
              "message": %s
            }
        """, authException!=null?authException.getMessage():"Authentication is required to access this resource."));
    }
}

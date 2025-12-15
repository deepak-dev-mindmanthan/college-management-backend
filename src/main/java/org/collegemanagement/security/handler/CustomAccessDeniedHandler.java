package org.collegemanagement.security.handler;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(@Nullable HttpServletRequest request,
                       HttpServletResponse response,
                       @Nullable AccessDeniedException ex) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");

        response.getWriter().write(String.format(
                """
                            {
                              "status": 403,
                              "error": "Forbidden",
                              "message": %s
                            }
                        """, ex != null ? ex.getMessage() : "You do not have permission to access this resource."
        ));
    }
}


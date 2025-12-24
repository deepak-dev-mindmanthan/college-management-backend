package org.collegemanagement.security.filter;


import jakarta.annotation.Nullable;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.security.tenant.TenantContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class TenantIsolationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @Nullable HttpServletRequest request,
            @Nullable HttpServletResponse response,
            @Nullable FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null
                    && authentication.isAuthenticated()
                    && authentication.getPrincipal() instanceof User user) {

                // Super admin has no tenant
                if (user.getCollege() != null) {
                    TenantContext.setTenantId(user.getCollege().getId());
                }
            }

            if (filterChain != null) {
                filterChain.doFilter(request, response);
            }

        } finally {
            // VERY IMPORTANT – prevent tenant leak
            TenantContext.clear();
        }
    }
}


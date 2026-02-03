package org.collegemanagement.security.filter;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.exception.factory.ApiErrorResponseFactory;
import org.collegemanagement.security.errors.SecurityErrorCode;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.security.tenant.TenantContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantIsolationFilter extends OncePerRequestFilter {

    private final TenantAccessGuard tenantAccessGuard;
    private final ObjectMapper objectMapper;
    private static final String TENANT_HEADER = "X-Tenant-ID";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof User user) {

                // Normal Tenant User → TenantId from DB only
                if (!tenantAccessGuard.isSuperAdmin()) {

                    if (user.getCollege() == null) {
                        sendErrorResponse(response, SecurityErrorCode.TENANT_CONTEXT_MISSING);
                        return;
                    }

                    Long tenantId = user.getCollege().getId();
                    TenantContext.setTenantId(tenantId);

                    log.debug("Tenant User Access → TenantId = {}", tenantId);
                }

                // Super Admin → TenantId must come from header
                else {
                    String tenantHeader = request.getHeader(TENANT_HEADER);

                    if (tenantHeader == null || tenantHeader.isBlank()) {
                        sendErrorResponse(response, SecurityErrorCode.TARGET_TENANT_REQUIRED);
                        return;
                    }

                    long tenantId;
                    try {
                        tenantId = Long.parseLong(tenantHeader);
                    } catch (NumberFormatException ex) {
                        sendErrorResponse(response, SecurityErrorCode.INVALID_TENANT);
                        return;
                    }

                    // Prevent invalid tenant IDs
                    if (tenantId <= 0) {
                        sendErrorResponse(response, SecurityErrorCode.INVALID_TENANT);
                        return;
                    }

                    TenantContext.setTenantId(tenantId);

                    log.warn("SUPER_ADMIN accessing TenantId = {}", tenantId);
                }
            }

            filterChain.doFilter(request, response);

        } finally {
            // VERY IMPORTANT – prevent tenant context leak between requests
            TenantContext.clear();
        }
    }

    private void sendErrorResponse(HttpServletResponse response, SecurityErrorCode code) throws IOException {
        var apiResponse = ApiErrorResponseFactory.from(SecurityErrorCode.TENANT_CONTEXT_MISSING);

        response.setStatus(code.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }
}


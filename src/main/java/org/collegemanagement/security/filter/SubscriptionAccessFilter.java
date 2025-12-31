package org.collegemanagement.security.filter;

import jakarta.annotation.Nullable;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.entity.subscription.Subscription;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.RoleType;
import org.collegemanagement.security.errors.SecurityErrorCode;
import org.collegemanagement.security.handler.CustomAccessDeniedHandler;
import org.collegemanagement.services.SubscriptionService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Filter to check subscription status before allowing API access.
 * <p>
 * Allows access to:
 * - Public endpoints (auth, pricing, swagger)
 * - SUPER_ADMIN (bypasses subscription check)
 * - Colleges with active subscriptions
 * <p>
 * Blocks access for:
 * - Colleges with expired/inactive subscriptions
 * - Colleges with no subscription
 */
@Component
@Slf4j
public class SubscriptionAccessFilter extends OncePerRequestFilter {

    private final SubscriptionService subscriptionService;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    public SubscriptionAccessFilter(@Lazy SubscriptionService subscriptionService, CustomAccessDeniedHandler accessDeniedHandler) {
        this.subscriptionService = subscriptionService;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    // Public endpoints that don't require subscription check
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/v1/auth",
            "/api/v1/subscription-plans",
            "/api/v1/pricing",
            "/swagger-ui",
            "/api/docs",
            "/api/v1/subscriptions",
            "/api/v1/invoices/generate",
            "/v3/api-docs",
            "/api/v1/payments/webhooks" // Webhooks should be accessible
    );

    @Override
    protected void doFilterInternal(
            @Nullable HttpServletRequest request,
            @Nullable HttpServletResponse response,
            @Nullable FilterChain filterChain
    ) throws ServletException, IOException {

        if (request == null || response == null || filterChain == null) {
            return;
        }

        String requestPath = request.getRequestURI();

        // Skip subscription check for public endpoints
        if (isPublicPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Skip check if not authenticated (will be handled by security filter)
            if (authentication == null || !authentication.isAuthenticated()) {
                filterChain.doFilter(request, response);
                return;
            }

            // SUPER_ADMIN bypasses subscription check
            if (isSuperAdmin(authentication)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Check subscription for college users
            if (authentication.getPrincipal() instanceof User user && user.getCollege() != null) {
                Long collegeId = user.getCollege().getId();

                // Check if subscription is active
                boolean hasActiveSubscription = subscriptionService.getSubscriptionByCollegeId(collegeId)
                        .map(Subscription::isActive)
                        .orElse(false);

                if (!hasActiveSubscription) {
                    log.warn("Access denied for college {} - subscription not active or expired", collegeId);
                    throw new AccessDeniedException(SecurityErrorCode.SUBSCRIPTION_EXPIRED.name());
                }
            }

            // Allow request to proceed
            filterChain.doFilter(request, response);

        } catch (AccessDeniedException e) {
            // Handle access denied exceptions
            accessDeniedHandler.handle(
                    request,
                    response,
                    new AccessDeniedException(SecurityErrorCode.SUBSCRIPTION_EXPIRED.name())
            );
        } catch (Exception e) {
            log.error("Error in subscription access filter: {}", e.getMessage(), e);
            // On error, allow request to proceed (fail open)
            // In production, you might want to fail closed
            filterChain.doFilter(request, response);
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isSuperAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> RoleType.ROLE_SUPER_ADMIN.name().equals(a.getAuthority()));
    }
}


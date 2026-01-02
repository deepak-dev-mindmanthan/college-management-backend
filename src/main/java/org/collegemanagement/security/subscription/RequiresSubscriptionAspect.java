package org.collegemanagement.security.subscription;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.enums.RoleType;
import org.collegemanagement.security.tenant.SubscriptionAccessGuard;
import org.collegemanagement.security.tenant.TenantContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RequiresSubscriptionAspect {

    private final SubscriptionAccessGuard subscriptionAccessGuard;

    @Before("@within(org.collegemanagement.security.subscription.RequiresSubscription) || " +
            "@annotation(org.collegemanagement.security.subscription.RequiresSubscription)")
    public void checkSubscription(JoinPoint joinPoint) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // No auth → let Spring Security handle it
        if (auth == null || !auth.isAuthenticated()) {
            return;
        }

        // SUPER_ADMIN bypasses subscription check
        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> RoleType.ROLE_SUPER_ADMIN.name().equals(a.getAuthority()));

        if (isSuperAdmin) {
            return;
        }

        Long collegeId = TenantContext.getTenantId();

        if (collegeId == null) {
            log.warn("Tenant context missing for subscription check");
            throw new org.springframework.security.access.AccessDeniedException(
                    org.collegemanagement.security.errors.SecurityErrorCode.TENANT_CONTEXT_MISSING.name()
            );
        }

        subscriptionAccessGuard.assertActiveSubscription(collegeId);
    }
}


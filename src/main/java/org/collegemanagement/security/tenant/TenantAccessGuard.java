package org.collegemanagement.security.tenant;

import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.enums.RoleType;
import org.collegemanagement.security.errors.SecurityErrorCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class TenantAccessGuard {

    /**
     * Enforce that the given College belongs to the current tenant.
     * SUPER_ADMIN bypasses this check.
     */
    public void assertCurrentTenant(College college) {

        // SUPER ADMIN → bypass
        if (isSuperAdmin()) {
            return;
        }

        if (college == null || college.getId() == null) {
            throw new AccessDeniedException(SecurityErrorCode.INVALID_TENANT.name());
        }

        Long currentTenantId = TenantContext.getTenantId();

        if (currentTenantId == null) {
            throw new AccessDeniedException(SecurityErrorCode.TENANT_CONTEXT_MISSING.name());
        }

        if (!college.getId().equals(currentTenantId)) {
            throw new AccessDeniedException(SecurityErrorCode.CROSS_COLLEGE_ACCESS.name());
        }
    }

    /**
     * Enforce tenant by collegeId directly.
     * SUPER_ADMIN bypasses this check.
     */
    public void assertCurrentTenantId(Long collegeId) {

        // SUPER ADMIN → bypass
        if (isSuperAdmin()) {
            return;
        }

        Long currentTenantId = TenantContext.getTenantId();

        if (currentTenantId == null || !currentTenantId.equals(collegeId)) {
            throw new AccessDeniedException(SecurityErrorCode.CROSS_COLLEGE_ACCESS.name());
        }
    }

    /**
     * Get current tenant ID with validation.
     * SUPER_ADMIN can access any tenant, but for others, tenant context must be set.
     * 
     * @return Current tenant ID
     * @throws AccessDeniedException if tenant context is missing (except for SUPER_ADMIN)
     */
    public Long getCurrentTenantId() {
        // SUPER ADMIN can work without tenant context
        if (isSuperAdmin()) {
            // For SUPER_ADMIN, return null or get from context if available
            return TenantContext.getTenantId();
        }

        Long currentTenantId = TenantContext.getTenantId();

        if (currentTenantId == null) {
            throw new AccessDeniedException(SecurityErrorCode.TENANT_CONTEXT_MISSING.name());
        }

        return currentTenantId;
    }

    /**
     * Centralized SUPER_ADMIN check
     */
    private boolean isSuperAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            return false;
        }

        return auth.getAuthorities().stream()
                .anyMatch(a -> RoleType.ROLE_SUPER_ADMIN.name().equals(a.getAuthority()));
    }
}

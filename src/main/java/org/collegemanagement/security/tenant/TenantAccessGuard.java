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

        if (isSuperAdmin()) {
            return;
        }

        if (college == null || college.getId() == null) {
            throw new AccessDeniedException(SecurityErrorCode.INVALID_TENANT.name());
        }

        Long tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            throw new AccessDeniedException(SecurityErrorCode.TENANT_CONTEXT_MISSING.name());
        }

        if (!college.getId().equals(tenantId)) {
            throw new AccessDeniedException(SecurityErrorCode.CROSS_COLLEGE_ACCESS.name());
        }
    }

    /**
     * Resolve collegeId depending on role.
     * SUPER_ADMIN must explicitly provide target collegeId.
     * COLLEGE_ADMIN uses tenant context.
     */
    public Long resolveCollegeId(Long requestedCollegeId) {

        if (isSuperAdmin()) {
            if (requestedCollegeId == null) {
                throw new AccessDeniedException(
                        SecurityErrorCode.TARGET_TENANT_REQUIRED.name()
                );
            }
            return requestedCollegeId;
        }

        return getCurrentTenantId();
    }

    public Long getCurrentTenantId() {

        Long tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            throw new AccessDeniedException(SecurityErrorCode.TENANT_CONTEXT_MISSING.name());
        }

        return tenantId;
    }

    /**
     * Centralized SUPER_ADMIN check
     */
    public boolean isSuperAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> RoleType.ROLE_SUPER_ADMIN.name().equals(a.getAuthority()));
    }
}


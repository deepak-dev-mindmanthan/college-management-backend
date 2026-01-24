package org.collegemanagement.security.tenant;

import org.collegemanagement.services.CollegeService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class TenantContextExecutor {

    private final CollegeService collegeService;
    private final TenantAccessGuard guard;


    public TenantContextExecutor(CollegeService collegeService, TenantAccessGuard guard) {
        this.collegeService = collegeService;
        this.guard = guard;
    }

    public <T> T callInTenant(String collegeUuid, Supplier<T> action) {

        if (!guard.isSuperAdmin()) {
            throw new AccessDeniedException("Tenant switching not allowed");
        }

        try {
            Long collegeId = collegeService.findByUuid(collegeUuid).getId();
            TenantContext.setTenantId(collegeId);
            return action.get();
        } finally {
            TenantContext.clear();
        }
    }

    public void runInTenant(String collegeUuid, Runnable action) {

        if (!guard.isSuperAdmin()) {
            throw new AccessDeniedException("Tenant switching not allowed");
        }

        try {
            Long collegeId = collegeService.findByUuid(collegeUuid).getId();
            TenantContext.setTenantId(collegeId);
            action.run();
        } finally {
            TenantContext.clear();
        }
    }
}


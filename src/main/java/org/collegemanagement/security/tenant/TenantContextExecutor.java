package org.collegemanagement.security.tenant;

import org.collegemanagement.services.CollegeService;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class TenantContextExecutor {

    private final CollegeService collegeService;

    public TenantContextExecutor(CollegeService collegeService) {
        this.collegeService = collegeService;
    }

    public <T> T callInTenant(String collegeUuid, Supplier<T> action) {
        try {
            Long collegeId = collegeService.findByUuid(collegeUuid).getId();
            TenantContext.setTenantId(collegeId);
            return action.get();
        } finally {
            TenantContext.clear();
        }
    }

    public void runInTenant(String collegeUuid, Runnable action) {
        try {
            Long collegeId = collegeService.findByUuid(collegeUuid).getId();
            TenantContext.setTenantId(collegeId);
            action.run();
        } finally {
            TenantContext.clear();
        }
    }
}


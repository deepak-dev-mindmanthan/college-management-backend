package org.collegemanagement.security.permission;


import jakarta.annotation.Nullable;
import org.collegemanagement.enums.RoleType;
import org.collegemanagement.security.errors.SecurityErrorCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Objects;

import org.collegemanagement.security.tenant.TenantContext;

@Component
public class CollegeIsolationPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(
            @Nullable Authentication authentication,
            @Nullable Object targetDomainObject,
            @Nullable Object permission
    ) {
        // Not used in this design
        return true;
    }

    @Override
    public boolean hasPermission(
            @Nullable Authentication authentication,
            @Nullable Serializable targetCollegeId,
            @Nullable String targetType,
            @Nullable Object permission
    ) {
        if (authentication == null) {
            throw new AccessDeniedException(
                    SecurityErrorCode.UN_AUTHENTICATED.name()
            );
        }

        // SUPER ADMIN → bypass tenant isolation
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> RoleType.ROLE_SUPER_ADMIN.name().equals(a.getAuthority()))) {
            return true;
        }

        Long currentTenantId = TenantContext.getTenantId();

        if (currentTenantId == null) {
            throw new AccessDeniedException(
                    SecurityErrorCode.TENANT_CONTEXT_MISSING.name()
            );
        }

        // If method provides a tenant id, enforce match
        if (targetCollegeId != null) {
            Long requestedTenantId = Long.valueOf(targetCollegeId.toString());

            if (!Objects.equals(currentTenantId, requestedTenantId)) {
                throw new AccessDeniedException(SecurityErrorCode.CROSS_COLLEGE_ACCESS.name());
            }
        }
        return true;
    }
}


//@Component
//public class CollegeIsolationPermissionEvaluator implements PermissionEvaluator {
//
//    @Override
//    public boolean hasPermission(
//            @Nullable Authentication authentication,
//            @Nullable Object targetDomainObject,
//            @Nullable Object permission
//    ) {
//        // Not used
//        return true;
//    }
//
//    @Override
//    public boolean hasPermission(
//            @Nullable Authentication authentication,
//            @Nullable Serializable targetCollegeId,
//            @Nullable String targetType,
//            @Nullable Object permission
//    ) {
//        if (authentication == null || targetCollegeId == null) {
//            throw new AccessDeniedException("CROSS_COLLEGE_ACCESS");
//
////            return false;
//        }
//
//        // SUPER ADMIN → allow all
//        if (authentication.getAuthorities().stream()
//                .anyMatch(a -> "ROLE_SUPER_ADMIN".equals(a.getAuthority()))) {
//            return true;
//        }
//
//        Object principal = authentication.getPrincipal();
//
//        // JWT-based auth
//        if (principal instanceof Jwt jwt) {
//            Long requestedCollegeId = Long.valueOf(targetCollegeId.toString());
//            Long userCollegeId = jwt.getClaim("collegeId");
//
//            if (!Objects.equals(userCollegeId, requestedCollegeId)) {
//                throw new AccessDeniedException("CROSS_COLLEGE_ACCESS");
//            }
//            return true;
//        }
//
//
//        if (principal instanceof User user && user.getCollege() != null) {
//            Long requestedCollegeId = Long.valueOf(targetCollegeId.toString());
//
//            if (!Objects.equals(user.getCollege().getId(), requestedCollegeId)) {
//                throw new AccessDeniedException("CROSS_COLLEGE_ACCESS");
//            }
//            return true;
//        }
//
//
//        return false;
//    }
//}
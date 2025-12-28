package org.collegemanagement.services.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.entity.audit.AuditLog;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.AuditAction;
import org.collegemanagement.enums.AuditEntityType;
import org.collegemanagement.repositories.AuditLogRepository;
import org.collegemanagement.repositories.UserRepository;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.AuditService;
import org.collegemanagement.services.CollegeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final TenantAccessGuard tenantAccessGuard;
    private final CollegeService collegeService;

    @Override
    @Transactional
    public AuditLog createAuditLog(Long userId, AuditAction action, AuditEntityType entityType, Long entityId, String description) {
        return createAuditLog(userId, action, entityType, entityId, description, getClientIpAddress());
    }

    @Override
    @Transactional
    public AuditLog createAuditLog(Long userId, AuditAction action, AuditEntityType entityType, Long entityId, String description, String ipAddress) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        College college = collegeService.findById(collegeId);

        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;

        AuditLog auditLog = AuditLog.builder()
                .college(college)
                .user(user)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .ipAddress(ipAddress)
                .build();

        return auditLogRepository.save(auditLog);
    }

    /**
     * Helper method to get client IP address from request
     */
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception e) {
            log.warn("Failed to get client IP address: {}", e.getMessage());
        }
        return null;
    }
}


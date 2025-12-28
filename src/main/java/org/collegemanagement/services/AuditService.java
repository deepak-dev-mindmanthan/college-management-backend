package org.collegemanagement.services;

import org.collegemanagement.entity.audit.AuditLog;
import org.collegemanagement.enums.AuditAction;
import org.collegemanagement.enums.AuditEntityType;

/**
 * Service for creating audit logs
 */
public interface AuditService {

    /**
     * Create an audit log entry
     */
    AuditLog createAuditLog(Long userId, AuditAction action, AuditEntityType entityType, Long entityId, String description);

    /**
     * Create an audit log entry with IP address
     */
    AuditLog createAuditLog(Long userId, AuditAction action, AuditEntityType entityType, Long entityId, String description, String ipAddress);
}


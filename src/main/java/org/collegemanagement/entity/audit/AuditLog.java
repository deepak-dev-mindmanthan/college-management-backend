package org.collegemanagement.entity.audit;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.enums.AuditAction;
import org.collegemanagement.enums.AuditEntityType;

import java.time.Instant;

@Entity
@Table(
        name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_college", columnList = "college_id"),
                @Index(name = "idx_audit_user", columnList = "user_id"),
                @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
                @Index(name = "idx_audit_action", columnList = "action"),
                @Index(name = "idx_audit_created", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AuditLog extends BaseEntity {

    /**
     * Tenant (College / School)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    /**
     * User who performed the action
     * (nullable for system actions)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Action performed
     * (CREATE, UPDATE, DELETE, LOGIN, LOGOUT, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AuditAction action;

    /**
     * Target entity type
     * (STUDENT, EXAM, FEE_PAYMENT, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 50)
    private AuditEntityType entityType;

    /**
     * ID of the affected entity
     */
    @Column(name = "entity_id")
    private Long entityId;

    /**
     * Source IP address
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * Optional descriptive message
     */
    @Column(length = 1000)
    private String description;

}


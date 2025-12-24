package org.collegemanagement.entity.support;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.TicketPriority;
import org.collegemanagement.enums.TicketStatus;

import java.time.Instant;

@Entity
@Table(
        name = "support_tickets",
        indexes = {
                @Index(name = "idx_ticket_college", columnList = "college_id"),
                @Index(name = "idx_ticket_status", columnList = "status"),
                @Index(name = "idx_ticket_priority", columnList = "priority"),
                @Index(name = "idx_ticket_raised_by", columnList = "raised_by"),
                @Index(name = "idx_ticket_created", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SupportTicket extends BaseEntity {

    /* =========================
       TENANT
       ========================= */

    /**
     * Tenant (College / School)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    /* =========================
       WHO RAISED THE TICKET
       ========================= */

    /**
     * User who raised the ticket
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "raised_by", nullable = false)
    private User raisedBy;

    /* =========================
       TICKET CONTENT
       ========================= */

    /**
     * Short subject/title
     */
    @Column(nullable = false, length = 200)
    private String subject;

    /**
     * Detailed description
     */
    @Column(nullable = false, length = 2000)
    private String description;

    /* =========================
       PRIORITY & STATUS
       ========================= */

    /**
     * LOW / MEDIUM / HIGH / CRITICAL
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority;

    /**
     * OPEN / IN_PROGRESS / RESOLVED / CLOSED
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    /* =========================
       LIFECYCLE (OPTIONAL BUT IMPORTANT)
       ========================= */

    /**
     * When ticket was closed
     */
    @Column(name = "closed_at")
    private Instant closedAt;

    /**
     * Who resolved/closed the ticket (support/admin)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = TicketStatus.OPEN;
        }
        if (priority == null) {
            priority = TicketPriority.MEDIUM;
        }
    }
}


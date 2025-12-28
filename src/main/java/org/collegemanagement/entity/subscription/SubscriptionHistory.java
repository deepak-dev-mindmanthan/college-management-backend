package org.collegemanagement.entity.subscription;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.enums.SubscriptionStatus;

import java.time.LocalDateTime;

/**
 * Entity to track subscription history and changes.
 * Provides audit trail for subscription modifications.
 */
@Entity
@Table(
        name = "subscription_history",
        indexes = {
                @Index(name = "idx_sub_history_subscription", columnList = "subscription_id"),
                @Index(name = "idx_sub_history_created", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SubscriptionHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus newStatus;

    @Column(length = 500)
    private String changeReason;

    @Column(name = "changed_by")
    private String changedBy; // User email or system

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}


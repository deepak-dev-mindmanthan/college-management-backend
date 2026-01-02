package org.collegemanagement.entity.subscription;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.finance.Invoice;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.enums.SubscriptionStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(
        name = "subscriptions",
        indexes = {
                @Index(name = "idx_subscription_college", columnList = "college_id"),
                @Index(name = "idx_subscription_status", columnList = "status"),
                @Index(name = "idx_subscription_expires", columnList = "expires_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder

/*
 * Represents the CURRENT active subscription for a college.
 * Historical subscriptions are stored in subscription_history.
 */
public class Subscription extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "plan_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_subscription_plan")
    )
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status = SubscriptionStatus.PENDING;


    @Column(name = "starts_at", nullable = false)
    private LocalDate startsAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDate expiresAt;

    /**
     * Grace period end date (if subscription has grace period after expiry)
     */
    @Column(name = "grace_period_ends_at")
    private LocalDate gracePeriodEndsAt;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "college_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_subscription_college")
    )
    private College college;

    @OneToMany(mappedBy = "subscription", fetch = FetchType.LAZY)
    private Set<Invoice> invoices;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    public boolean isActive() {
        LocalDate now = LocalDate.now();
        // Active if status is ACTIVE and not expired (or within grace period)
        if (status == SubscriptionStatus.ACTIVE) {
            if (!expiresAt.isBefore(now)) {
                return true; // Not expired yet
            }
            // Check grace period
            if (gracePeriodEndsAt != null && !gracePeriodEndsAt.isBefore(now)) {
                return true; // Within grace period
            }
        }
        return false;
    }

    public boolean isExpired() {
        LocalDate now = LocalDate.now();
        if (expiresAt.isBefore(now)) {
            // Check if still in grace period
            if (gracePeriodEndsAt != null && !gracePeriodEndsAt.isBefore(now)) {
                return false; // Still in grace period
            }
            return true; // Expired and grace period passed
        }
        return false;
    }

    /**
     * Check if subscription is in grace period
     */
    public boolean isInGracePeriod() {
        LocalDate now = LocalDate.now();
        return expiresAt.isBefore(now) 
                && gracePeriodEndsAt != null 
                && !gracePeriodEndsAt.isBefore(now);
    }

    /**
     * Access allowed when active or in grace period
     */
    public boolean isUsable() {
        return isActive() || isInGracePeriod();
    }

}

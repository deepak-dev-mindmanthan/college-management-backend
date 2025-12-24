package org.collegemanagement.entity.subscription;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.finance.Invoice;
import org.collegemanagement.entity.base.BaseEntity;
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
        return status == SubscriptionStatus.ACTIVE
                && !expiresAt.isBefore(LocalDate.now());
    }

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDate.now());
    }

    /**
     * Access allowed ONLY when active
     */
    public boolean isUsable() {
        return isActive();
    }

}

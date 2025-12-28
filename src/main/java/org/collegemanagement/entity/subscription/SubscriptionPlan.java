package org.collegemanagement.entity.subscription;

import jakarta.persistence.*;
import lombok.*;
import org.collegemanagement.enums.BillingCycle;
import org.collegemanagement.enums.CurrencyCode;
import org.collegemanagement.enums.SubscriptionPlanType;

import java.math.BigDecimal;

@Entity
@Table(
        name = "subscription_plans",
        indexes = {
                @Index(name = "idx_plan_code", columnList = "code"),
                @Index(name = "idx_plan_active", columnList = "is_active")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_plan_code_cycle",
                        columnNames = {"code", "billing_cycle"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * STARTER, STANDARD, PREMIUM
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SubscriptionPlanType code;

    /**
     * MONTHLY, QUARTERLY, YEARLY
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false, length = 20)
    private BillingCycle billingCycle;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CurrencyCode currency;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    /**
     * Plan limits and features
     */
    @Column(name = "max_students")
    private Integer maxStudents;

    @Column(name = "max_teachers")
    private Integer maxTeachers;

    @Column(name = "max_departments")
    private Integer maxDepartments;

    @Column(name = "max_storage_gb")
    private Integer maxStorageGB;

    @Column(name = "grace_period_days")
    private Integer gracePeriodDays = 7; // Default 7 days grace period after expiry

    /**
     * Feature flags enabled for this plan
     * Many-to-Many relationship with FeatureFlag
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "subscription_plan_features",
            joinColumns = @JoinColumn(name = "plan_id"),
            inverseJoinColumns = @JoinColumn(name = "feature_key", referencedColumnName = "feature_key")
    )
    private java.util.Set<org.collegemanagement.entity.config.FeatureFlag> enabledFeatures;
}

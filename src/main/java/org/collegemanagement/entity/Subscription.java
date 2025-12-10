package org.collegemanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.collegemanagement.enums.BillingCycle;
import org.collegemanagement.enums.SubscriptionPlan;
import org.collegemanagement.enums.SubscriptionStatus;
import org.collegemanagement.enums.CurrencyCode;

import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingCycle billingCycle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal priceAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyCode currency = CurrencyCode.USD;

    @Column(nullable = false)
    private LocalDate startsAt;

    @Column(nullable = false)
    private LocalDate expiresAt;

    @OneToOne
    @JoinColumn(name = "college_id", nullable = false, unique = true)
    @JsonIgnore
    private College college;

    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE && !expiresAt.isBefore(LocalDate.now());
    }
}


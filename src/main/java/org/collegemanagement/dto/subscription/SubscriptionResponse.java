package org.collegemanagement.dto.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.collegemanagement.enums.BillingCycle;
import org.collegemanagement.enums.CurrencyCode;
import org.collegemanagement.enums.SubscriptionPlanType;
import org.collegemanagement.enums.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionResponse {
    private String uuid;
    private SubscriptionPlanType planType;
    private BillingCycle billingCycle;
    private BigDecimal price;
    private CurrencyCode currency;
    private SubscriptionStatus status;
    private LocalDate startsAt;
    private LocalDate expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long collegeId;
    private String collegeName;
    private Long invoiceCount;
    private boolean isActive;
    private boolean isExpired;
    private Long daysRemaining;
}


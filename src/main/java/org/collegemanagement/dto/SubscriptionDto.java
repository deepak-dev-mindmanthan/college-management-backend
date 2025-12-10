package org.collegemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.collegemanagement.entity.Subscription;
import org.collegemanagement.enums.BillingCycle;
import org.collegemanagement.enums.SubscriptionPlan;
import org.collegemanagement.enums.SubscriptionStatus;
import org.collegemanagement.enums.CurrencyCode;

import java.time.LocalDate;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionDto {
    private Long id;
    private SubscriptionPlan plan;
    private BillingCycle billingCycle;
    private SubscriptionStatus status;
    private BigDecimal priceAmount;
    private CurrencyCode currency;
    private LocalDate startsAt;
    private LocalDate expiresAt;

    public static SubscriptionDto fromEntity(Subscription subscription) {
        if (subscription == null) {
            return null;
        }
        return SubscriptionDto.builder()
                .id(subscription.getId())
                .plan(subscription.getPlan())
                .billingCycle(subscription.getBillingCycle())
                .status(subscription.getStatus())
                .priceAmount(subscription.getPriceAmount())
                .currency(subscription.getCurrency())
                .startsAt(subscription.getStartsAt())
                .expiresAt(subscription.getExpiresAt())
                .build();
    }
}


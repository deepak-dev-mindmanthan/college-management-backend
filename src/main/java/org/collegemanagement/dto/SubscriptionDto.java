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

import java.time.LocalDate;

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
                .startsAt(subscription.getStartsAt())
                .expiresAt(subscription.getExpiresAt())
                .build();
    }
}


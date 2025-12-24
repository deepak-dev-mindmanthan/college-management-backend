package org.collegemanagement.mapper;

import lombok.NoArgsConstructor;
import org.collegemanagement.dto.SubscriptionSummary;
import org.collegemanagement.entity.subscription.Subscription;
import org.collegemanagement.enums.SubscriptionPlanType;


@NoArgsConstructor
public final class SubscriptionMapper {

    public static SubscriptionSummary toSummary(Subscription subscription) {

        if (subscription == null) {
            return SubscriptionSummary.builder()
                    .plan(SubscriptionPlanType.NONE)
                    .canAccessCoreApis(false)
                    .build();
        }

        return SubscriptionSummary.builder()
                .plan(subscription.getPlan().getCode())
                .expiresAt(subscription.getExpiresAt())
                .canAccessCoreApis(subscription.isUsable())
                .build();
    }
}

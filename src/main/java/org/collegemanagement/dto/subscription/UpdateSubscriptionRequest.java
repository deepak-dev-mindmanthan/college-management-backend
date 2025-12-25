package org.collegemanagement.dto.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.collegemanagement.enums.BillingCycle;
import org.collegemanagement.enums.SubscriptionPlanType;
import org.collegemanagement.enums.SubscriptionStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSubscriptionRequest {

    private SubscriptionPlanType planType;
    private BillingCycle billingCycle;
    private SubscriptionStatus status;
}


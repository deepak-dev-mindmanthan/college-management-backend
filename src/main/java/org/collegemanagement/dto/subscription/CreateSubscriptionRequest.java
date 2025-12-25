package org.collegemanagement.dto.subscription;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.collegemanagement.enums.BillingCycle;
import org.collegemanagement.enums.SubscriptionPlanType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSubscriptionRequest {

    @NotNull(message = "Plan type is required")
    private SubscriptionPlanType planType;

    @NotNull(message = "Billing cycle is required")
    private BillingCycle billingCycle;
}


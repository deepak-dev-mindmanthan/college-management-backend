package org.collegemanagement.dto.subscription;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.collegemanagement.enums.BillingCycle;
import org.collegemanagement.enums.SubscriptionPlanType;


@Getter
@Setter
public class UpgradeSubscriptionRequest {

    @NotNull
    private SubscriptionPlanType planType;

    @NotNull
    private BillingCycle billingCycle;
}


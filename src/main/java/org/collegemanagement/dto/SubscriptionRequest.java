package org.collegemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.collegemanagement.enums.BillingCycle;
import org.collegemanagement.enums.SubscriptionPlan;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionRequest {
    private SubscriptionPlan plan;
    private BillingCycle billingCycle;
}


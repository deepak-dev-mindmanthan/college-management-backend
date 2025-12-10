package org.collegemanagement.dto;

import lombok.Getter;
import lombok.Setter;
import org.collegemanagement.enums.BillingCycle;
import org.collegemanagement.enums.SubscriptionPlan;

@Getter
@Setter
public class PaymentRequest {
    private Long collegeId;
    private SubscriptionPlan plan;
    private BillingCycle billingCycle;
}


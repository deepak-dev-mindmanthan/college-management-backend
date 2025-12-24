package org.collegemanagement.dto;

import lombok.Getter;
import lombok.Setter;
import org.collegemanagement.enums.BillingCycle;
import org.collegemanagement.enums.SubscriptionPlanType;

@Getter
@Setter
public class PaymentRequest {
    private Long collegeId;
    private SubscriptionPlanType plan;
    private BillingCycle billingCycle;
}


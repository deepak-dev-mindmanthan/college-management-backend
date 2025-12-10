package org.collegemanagement.dto;

import lombok.Getter;
import lombok.Setter;
import org.collegemanagement.enums.BillingCycle;
import org.collegemanagement.enums.SubscriptionPlan;

import java.math.BigDecimal;

@Getter
@Setter
public class PlanPriceRequest {
    private SubscriptionPlan plan;
    private BillingCycle billingCycle;
    private BigDecimal amount;
    private String currency;
    private boolean active = true;
}


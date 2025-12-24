package org.collegemanagement.dto;

import lombok.Getter;
import lombok.Setter;
import org.collegemanagement.enums.BillingCycle;
import org.collegemanagement.enums.SubscriptionPlanType;
import org.collegemanagement.enums.CurrencyCode;

import java.math.BigDecimal;

@Getter
@Setter
public class PlanPriceRequest {
    private SubscriptionPlanType plan;
    private BillingCycle billingCycle;
    private BigDecimal amount;
    private CurrencyCode currency;
    private boolean active = true;
}


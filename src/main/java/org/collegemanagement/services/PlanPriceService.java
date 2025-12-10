package org.collegemanagement.services;

import org.collegemanagement.entity.PlanPrice;
import org.collegemanagement.enums.BillingCycle;
import org.collegemanagement.enums.SubscriptionPlan;
import org.collegemanagement.enums.CurrencyCode;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

public interface PlanPriceService {
    PlanPrice upsert(SubscriptionPlan plan, BillingCycle billingCycle, BigDecimal amount, CurrencyCode currency, boolean active);
    Optional<PlanPrice> findActivePrice(SubscriptionPlan plan, BillingCycle billingCycle);
    List<PlanPrice> listActive();
}


package org.collegemanagement.services;


import org.collegemanagement.entity.subscription.SubscriptionPlan;
import org.collegemanagement.enums.BillingCycle;
import org.collegemanagement.enums.SubscriptionPlanType;

import java.util.List;

public interface SubscriptionPlanService {

    List<SubscriptionPlan> getActivePlans();

    SubscriptionPlan getActivePlan(
            SubscriptionPlanType planType,
            BillingCycle billingCycle
    );

    SubscriptionPlan createPlan(SubscriptionPlan plan);

    SubscriptionPlan deactivatePlan(Long planId);
}
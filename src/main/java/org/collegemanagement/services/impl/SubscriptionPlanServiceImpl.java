package org.collegemanagement.services.impl;



import lombok.RequiredArgsConstructor;
import org.collegemanagement.entity.subscription.SubscriptionPlan;
import org.collegemanagement.enums.BillingCycle;
import org.collegemanagement.enums.SubscriptionPlanType;
import org.collegemanagement.repositories.SubscriptionPlanRepository;
import org.collegemanagement.services.SubscriptionPlanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionPlanServiceImpl
        implements SubscriptionPlanService {

    private final SubscriptionPlanRepository planRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPlan> getActivePlans() {
        return planRepository.findByActiveTrueOrderByPriceAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionPlan getActivePlan(
            SubscriptionPlanType planType,
            BillingCycle billingCycle
    ) {
        return planRepository
                .findByCodeAndBillingCycleAndActiveTrue(planType, billingCycle)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Active plan not found: " + planType + " / " + billingCycle
                        )
                );
    }

    @Override
    public SubscriptionPlan createPlan(SubscriptionPlan plan) {

        if (planRepository.existsByCodeAndBillingCycle(
                plan.getCode(),
                plan.getBillingCycle()
        )) {
            throw new IllegalStateException(
                    "Plan already exists for given code and billing cycle"
            );
        }

        plan.setActive(true);
        return planRepository.save(plan);
    }

    @Override
    public SubscriptionPlan deactivatePlan(Long planId) {

        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Plan not found")
                );

        plan.setActive(false);
        return plan;
    }
}

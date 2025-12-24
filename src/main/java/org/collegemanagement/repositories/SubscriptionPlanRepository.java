package org.collegemanagement.repositories;

import org.collegemanagement.entity.subscription.SubscriptionPlan;
import org.collegemanagement.enums.BillingCycle;
import org.collegemanagement.enums.SubscriptionPlanType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanRepository
        extends JpaRepository<SubscriptionPlan, Long> {

    List<SubscriptionPlan> findByActiveTrueOrderByPriceAsc();

    Optional<SubscriptionPlan> findByCodeAndBillingCycleAndActiveTrue(
            SubscriptionPlanType code,
            BillingCycle billingCycle
    );

    boolean existsByCodeAndBillingCycle(
            SubscriptionPlanType code,
            BillingCycle billingCycle
    );
}

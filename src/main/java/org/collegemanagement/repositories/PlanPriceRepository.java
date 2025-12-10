package org.collegemanagement.repositories;

import org.collegemanagement.entity.PlanPrice;
import org.collegemanagement.enums.BillingCycle;
import org.collegemanagement.enums.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanPriceRepository extends JpaRepository<PlanPrice, Long> {
    Optional<PlanPrice> findByPlanAndBillingCycleAndActiveTrue(SubscriptionPlan plan, BillingCycle billingCycle);
    List<PlanPrice> findByActiveTrue();
}


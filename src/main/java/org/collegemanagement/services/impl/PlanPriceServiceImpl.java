package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import org.collegemanagement.entity.PlanPrice;
import org.collegemanagement.enums.BillingCycle;
import org.collegemanagement.enums.SubscriptionPlan;
import org.collegemanagement.repositories.PlanPriceRepository;
import org.collegemanagement.services.PlanPriceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanPriceServiceImpl implements PlanPriceService {

    private final PlanPriceRepository planPriceRepository;

    @Transactional
    @Override
    public PlanPrice upsert(SubscriptionPlan plan, BillingCycle billingCycle, BigDecimal amount, String currency, boolean active) {
        Optional<PlanPrice> existing = planPriceRepository.findByPlanAndBillingCycleAndActiveTrue(plan, billingCycle);
        PlanPrice price = existing.orElse(PlanPrice.builder()
                .plan(plan)
                .billingCycle(billingCycle)
                .build());
        price.setAmount(amount);
        price.setCurrency(currency != null ? currency : "USD");
        price.setActive(active);
        return planPriceRepository.save(price);
    }

    @Override
    public Optional<PlanPrice> findActivePrice(SubscriptionPlan plan, BillingCycle billingCycle) {
        return planPriceRepository.findByPlanAndBillingCycleAndActiveTrue(plan, billingCycle);
    }

    @Override
    public List<PlanPrice> listActive() {
        return planPriceRepository.findByActiveTrue();
    }
}


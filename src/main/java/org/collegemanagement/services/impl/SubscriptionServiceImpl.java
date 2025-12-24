package org.collegemanagement.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.collegemanagement.dto.SubscriptionRequest;
import org.collegemanagement.dto.SubscriptionStatusResponse;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.subscription.Subscription;
import org.collegemanagement.entity.subscription.SubscriptionPlan;
import org.collegemanagement.enums.*;
import org.collegemanagement.repositories.SubscriptionRepository;
import org.collegemanagement.services.SubscriptionPlanService;
import org.collegemanagement.services.SubscriptionService;
import org.collegemanagement.utils.SubscriptionDateCalculator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanService subscriptionPlanService;

    @Transactional
    @Override
    public Subscription createSubscriptionForCollege(College college, SubscriptionRequest request) {

        SubscriptionPlan plan = subscriptionPlanService.getActivePlan(request.getPlan(), request.getBillingCycle());

        Subscription subscription = Subscription.builder()
                .college(college)
                .plan(plan)
                .startsAt(LocalDate.now())
                .expiresAt(
                        SubscriptionDateCalculator.calculateExpiry(
                                LocalDate.now(),
                                plan.getBillingCycle()
                        )
                )
                .build();
        return subscriptionRepository.save(subscription);
    }



    @Override
    public Optional<Subscription> getSubscriptionByCollegeId(Long collegeId) {
        return subscriptionRepository.findByCollegeId(collegeId);
    }

    public SubscriptionStatusResponse getStatus(College college) {

        if (college.getSubscription() == null || college.getSubscription().getStatus() != SubscriptionStatus.ACTIVE) {
            return SubscriptionStatusResponse.builder()
                    .status(SubscriptionStatus.NONE)
                    .planType(SubscriptionPlanType.NONE)
                    .expiresAt(null)
                    .canAccessCoreApis(false)
                    .build();
        }

        Optional<Subscription> optionalSubscription =
                subscriptionRepository.findByCollegeId(college.getId());

        if (optionalSubscription.isEmpty()) {
            // No subscription purchased yet
            return SubscriptionStatusResponse.builder()
                    .status(SubscriptionStatus.NONE)
                    .planType(SubscriptionPlanType.NONE)
                    .expiresAt(null)
                    .canAccessCoreApis(false)
                    .build();
        }

        Subscription subscription = optionalSubscription.get();

        boolean canAccess = subscription.isActive();

        return SubscriptionStatusResponse.builder()
                .status(subscription.getStatus())
                .planType(subscription.getPlan().getCode())
                .expiresAt(subscription.getExpiresAt())
                .canAccessCoreApis(canAccess)
                .build();
    }


}



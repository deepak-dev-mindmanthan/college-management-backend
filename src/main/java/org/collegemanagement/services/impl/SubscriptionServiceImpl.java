package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import org.collegemanagement.dto.SubscriptionRequest;
import org.collegemanagement.entity.College;
import org.collegemanagement.entity.Subscription;
import org.collegemanagement.entity.User;
import org.collegemanagement.enums.BillingCycle;
import org.collegemanagement.enums.RoleType;
import org.collegemanagement.enums.SubscriptionPlan;
import org.collegemanagement.enums.SubscriptionStatus;
import org.collegemanagement.repositories.SubscriptionRepository;
import org.collegemanagement.services.SubscriptionService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    @Override
    public Subscription createOrUpdateForCollege(College college, SubscriptionRequest request) {
        if (request == null) {
            request = SubscriptionRequest.builder().build();
        }
        SubscriptionPlan plan = Optional.ofNullable(request.getPlan()).orElse(SubscriptionPlan.STARTER);
        BillingCycle billingCycle = Optional.ofNullable(request.getBillingCycle()).orElse(BillingCycle.MONTHLY);

        List<Subscription> active = subscriptionRepository.findByCollegeIdAndStatusIn(
                college.getId(),
                List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIAL)
        );
        active.forEach(subscription -> subscription.setStatus(SubscriptionStatus.CANCELLED));
        subscriptionRepository.saveAll(active);

        LocalDate start = LocalDate.now();
        LocalDate expiry = billingCycle == BillingCycle.ANNUAL ? start.plusYears(1) : start.plusMonths(1);

        Subscription subscription = Subscription.builder()
                .plan(plan)
                .billingCycle(billingCycle)
                .status(SubscriptionStatus.ACTIVE)
                .startsAt(start)
                .expiresAt(expiry)
                .college(college)
                .build();

        return subscriptionRepository.save(subscription);
    }

    @Override
    public Subscription getActiveSubscription(Long collegeId) {
        if (collegeId == null) {
            return null;
        }
        Optional<Subscription> subscriptionOptional = subscriptionRepository.findTopByCollegeIdAndStatusInOrderByExpiresAtDesc(
                collegeId, List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIAL)
        );
        if (subscriptionOptional.isEmpty()) {
            return null;
        }

        Subscription subscription = subscriptionOptional.get();
        if (subscription.getExpiresAt().isBefore(LocalDate.now())) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);
            return null;
        }
        return subscription;
    }

    @Override
    public Subscription ensureActiveSubscription(User user) {
        boolean isSuperAdmin = user.getRoles().stream().anyMatch(role -> role.getName() == RoleType.ROLE_SUPER_ADMIN);
        if (isSuperAdmin) {
            return null;
        }
        if (user.getCollege() == null) {
            throw new BadCredentialsException("User is not assigned to a college subscription.");
        }
        Subscription subscription = getActiveSubscription(user.getCollege().getId());
        if (subscription == null) {
            throw new BadCredentialsException("No active subscription for this college.");
        }
        return subscription;
    }
}



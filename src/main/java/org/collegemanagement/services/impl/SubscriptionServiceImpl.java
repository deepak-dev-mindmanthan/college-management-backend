package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import org.collegemanagement.dto.SubscriptionRequest;
import org.collegemanagement.entity.College;
import org.collegemanagement.entity.PlanPrice;
import org.collegemanagement.entity.Subscription;
import org.collegemanagement.entity.User;
import org.collegemanagement.enums.BillingCycle;
import org.collegemanagement.enums.RoleType;
import org.collegemanagement.enums.SubscriptionPlan;
import org.collegemanagement.enums.SubscriptionStatus;
import org.collegemanagement.enums.CurrencyCode;
import org.collegemanagement.repositories.SubscriptionRepository;
import org.collegemanagement.services.PlanPriceService;
import org.collegemanagement.services.SubscriptionService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanPriceService planPriceService;
    private static final Map<SubscriptionPlan, Map<BillingCycle, BigDecimal>> DEFAULT_PRICES = new EnumMap<>(SubscriptionPlan.class);
    private static final CurrencyCode DEFAULT_CURRENCY = CurrencyCode.USD;

    static {
        Map<BillingCycle, BigDecimal> starter = new EnumMap<>(BillingCycle.class);
        starter.put(BillingCycle.MONTHLY, new BigDecimal("49.00"));
        starter.put(BillingCycle.ANNUAL, new BigDecimal("490.00"));

        Map<BillingCycle, BigDecimal> standard = new EnumMap<>(BillingCycle.class);
        standard.put(BillingCycle.MONTHLY, new BigDecimal("99.00"));
        standard.put(BillingCycle.ANNUAL, new BigDecimal("990.00"));

        Map<BillingCycle, BigDecimal> premium = new EnumMap<>(BillingCycle.class);
        premium.put(BillingCycle.MONTHLY, new BigDecimal("199.00"));
        premium.put(BillingCycle.ANNUAL, new BigDecimal("1990.00"));

        DEFAULT_PRICES.put(SubscriptionPlan.STARTER, starter);
        DEFAULT_PRICES.put(SubscriptionPlan.STANDARD, standard);
        DEFAULT_PRICES.put(SubscriptionPlan.PREMIUM, premium);
    }


    private PlanPrice resolvePrice(SubscriptionPlan plan, BillingCycle billingCycle) {
        Optional<PlanPrice> active = planPriceService.findActivePrice(plan, billingCycle);
        if (active.isPresent()) {
            return active.get();
        }
        BigDecimal defaultAmount = Optional.ofNullable(DEFAULT_PRICES.get(plan))
                .map(m -> m.get(billingCycle))
                .orElseThrow(() -> new IllegalStateException("No default price configured for " + plan + " / " + billingCycle));
        // Seed default so it becomes editable later
        return planPriceService.upsert(plan, billingCycle, defaultAmount, DEFAULT_CURRENCY, true);
    }

    @Transactional
    @Override
    public Subscription createOrUpdateForCollege(College college, SubscriptionRequest request) {
        if (request == null) {
            request = SubscriptionRequest.builder().build();
        }
        SubscriptionPlan plan = Optional.ofNullable(request.getPlan()).orElse(SubscriptionPlan.STARTER);
        BillingCycle billingCycle = Optional.ofNullable(request.getBillingCycle()).orElse(BillingCycle.MONTHLY);

        PlanPrice price = resolvePrice(plan, billingCycle);

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
                .priceAmount(price.getAmount())
                .currency(price.getCurrency())
                .startsAt(start)
                .expiresAt(expiry)
                .college(college)
                .build();

        Subscription currentSubscription = subscriptionRepository.findSubscriptionByCollegeId(college.getId());

        if (currentSubscription != null) {
            subscription.setId(currentSubscription.getId());
        }

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



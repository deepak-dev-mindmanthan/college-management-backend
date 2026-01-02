package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.subscription.*;
import org.collegemanagement.entity.subscription.Subscription;
import org.collegemanagement.entity.subscription.SubscriptionPlan;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.enums.BillingCycle;
import org.collegemanagement.enums.SubscriptionStatus;
import org.collegemanagement.exception.ResourceConflictException;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.repositories.CollegeRepository;
import org.collegemanagement.repositories.InvoiceRepository;
import org.collegemanagement.repositories.SubscriptionHistoryRepository;
import org.collegemanagement.repositories.SubscriptionRepository;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.EmailService;
import org.collegemanagement.services.SubscriptionPlanService;
import org.collegemanagement.services.SubscriptionService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final String EXISTING_SUB_MSG =
            "A subscription already exists for this college. Please use the upgrade or renew subscription endpoint.";

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanService subscriptionPlanService;
    private final CollegeRepository collegeRepository;
    private final InvoiceRepository invoiceRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final TenantAccessGuard tenantAccessGuard;
    private final EmailService emailService;

    /* ============================================================
       CREATE
       ============================================================ */

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public SubscriptionResponse createSubscription(CreateSubscriptionRequest request) {

        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        if (subscriptionRepository.existsByCollegeId(collegeId)) {
            throw new ResourceConflictException(EXISTING_SUB_MSG);
        }

        try {
            SubscriptionPlan plan =
                    subscriptionPlanService.getActivePlan(request.getPlanType(), request.getBillingCycle());

            College college = collegeRepository.findById(collegeId)
                    .orElseThrow(() -> new ResourceNotFoundException("College not found"));

            LocalDate startsAt = LocalDate.now();
            LocalDate expiresAt = calculateExpiryDate(startsAt, request.getBillingCycle());

            LocalDate gracePeriodEndsAt =
                    plan.getGracePeriodDays() != null && plan.getGracePeriodDays() > 0
                            ? expiresAt.plusDays(plan.getGracePeriodDays())
                            : null;

            Subscription subscription = Subscription.builder()
                    .plan(plan)
                    .college(college)
                    .status(SubscriptionStatus.PENDING)
                    .startsAt(startsAt)
                    .expiresAt(expiresAt)
                    .gracePeriodEndsAt(gracePeriodEndsAt)
                    .build();

            subscription = subscriptionRepository.save(subscription);

            college.setSubscription(subscription);
            collegeRepository.save(college);

            logSubscriptionHistory(
                    subscription,
                    SubscriptionStatus.NONE,
                    SubscriptionStatus.PENDING,
                    "Subscription created",
                    getCurrentUserEmail()
            );

            return mapToResponse(subscription);

        } catch (DataIntegrityViolationException ex) {
            throw new ResourceConflictException(EXISTING_SUB_MSG);
        }
    }


    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public SubscriptionResponse upgradeSubscription(
            String subscriptionUuid,
            UpgradeSubscriptionRequest request
    ) {
        Subscription subscription = getSubscriptionForTenant(subscriptionUuid);

        // Only ACTIVE subscriptions can be upgraded
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new ResourceConflictException(
                    "Only ACTIVE subscriptions can be upgraded"
            );
        }

        SubscriptionPlan currentPlan = subscription.getPlan();

        SubscriptionPlan newPlan =
                subscriptionPlanService.getActivePlan(
                        request.getPlanType(),
                        request.getBillingCycle()
                );

        //  Same plan protection
        if (currentPlan.getId().equals(newPlan.getId())) {
            throw new ResourceConflictException(
                    "Subscription is already on the selected plan"
            );
        }

        //  Downgrade protection (simple price-based rule)
        if (newPlan.getPrice().compareTo(currentPlan.getPrice()) < 0) {
            throw new ResourceConflictException(
                    "Downgrading plans is not allowed. Please contact support."
            );
        }

        SubscriptionStatus previousStatus = subscription.getStatus();

        //  Upgrade logic
        subscription.setPlan(newPlan);
        subscription.setStatus(SubscriptionStatus.PENDING);

        // Keep existing dates (important)
        // startsAt stays same
        // expiresAt stays same (new expiry after payment if needed)

        subscription = subscriptionRepository.save(subscription);

        //  History
        logSubscriptionHistory(
                subscription,
                previousStatus,
                SubscriptionStatus.PENDING,
                "Subscription upgraded from "
                        + currentPlan.getCode().name()
                        + " to "
                        + newPlan.getCode().name(),
                getCurrentUserEmail()
        );

        return mapToResponse(subscription);
    }

    /* ============================================================
       UPDATE (restricted)
       ============================================================ */

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public SubscriptionResponse updateSubscription(String subscriptionUuid, UpdateSubscriptionRequest request) {

        Subscription subscription = getSubscriptionForTenant(subscriptionUuid);

        if (subscription.isActive() &&
                (request.getPlanType() != null || request.getBillingCycle() != null)) {
            throw new ResourceConflictException(
                    "Active subscriptions must be upgraded using the upgrade subscription endpoint"
            );
        }

        if (request.getPlanType() != null && request.getBillingCycle() != null) {
            SubscriptionPlan plan =
                    subscriptionPlanService.getActivePlan(request.getPlanType(), request.getBillingCycle());

            subscription.setPlan(plan);
            subscription.setExpiresAt(
                    calculateExpiryDate(subscription.getStartsAt(), request.getBillingCycle())
            );
        }

        if (request.getStatus() != null) {
            subscription.setStatus(request.getStatus());
        }

        return mapToResponse(subscriptionRepository.save(subscription));
    }

    /* ============================================================
       CANCEL
       ============================================================ */

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public SubscriptionResponse cancelSubscription(String subscriptionUuid) {

        Subscription subscription = getSubscriptionForTenant(subscriptionUuid);
        SubscriptionStatus previousStatus = subscription.getStatus();

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription = subscriptionRepository.save(subscription);

        logSubscriptionHistory(
                subscription,
                previousStatus,
                SubscriptionStatus.CANCELLED,
                "Subscription cancelled",
                getCurrentUserEmail()
        );

        return mapToResponse(subscription);
    }

    /* ============================================================
       RENEW
       ============================================================ */

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public SubscriptionResponse renewSubscription(String subscriptionUuid, RenewSubscriptionRequest request) {

        Subscription subscription = getSubscriptionForTenant(subscriptionUuid);
        SubscriptionStatus previousStatus = subscription.getStatus();

        SubscriptionPlan plan =
                subscriptionPlanService.getActivePlan(request.getPlanType(), request.getBillingCycle());

        LocalDate startsAt =
                subscription.getExpiresAt().isAfter(LocalDate.now())
                        ? subscription.getExpiresAt()
                        : LocalDate.now();

        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.PENDING);
        subscription.setStartsAt(startsAt);
        subscription.setExpiresAt(calculateExpiryDate(startsAt, request.getBillingCycle()));

        subscription = subscriptionRepository.save(subscription);

        logSubscriptionHistory(
                subscription,
                previousStatus,
                SubscriptionStatus.PENDING,
                "Subscription renewed",
                getCurrentUserEmail()
        );

        return mapToResponse(subscription);
    }

    /* ============================================================
       ACTIVATE
       ============================================================ */

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public SubscriptionResponse activateSubscription(String subscriptionUuid) {

        Subscription subscription = getSubscriptionForTenant(subscriptionUuid);

        if (subscription.getStatus() != SubscriptionStatus.PENDING) {
            throw new ResourceConflictException("Only PENDING subscriptions can be activated");
        }

        SubscriptionStatus previousStatus = subscription.getStatus();
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription = subscriptionRepository.save(subscription);

        logSubscriptionHistory(
                subscription,
                previousStatus,
                SubscriptionStatus.ACTIVE,
                "Subscription activated",
                getCurrentUserEmail()
        );

        try {
            emailService.sendSubscriptionActivatedEmail(
                    subscription.getCollege().getEmail(),
                    subscription.getCollege().getName(),
                    subscription.getPlan().getCode().name(),
                    subscription.getExpiresAt()
            );
        } catch (Exception e) {
            log.warn("Failed to send activation email: {}", e.getMessage());
        }

        return mapToResponse(subscription);
    }

    /* ============================================================
       READ APIs
       ============================================================ */

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public SubscriptionResponse getSubscriptionByUuid(String subscriptionUuid) {
        return mapToResponse(getSubscriptionForTenant(subscriptionUuid));
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public SubscriptionResponse getCurrentSubscription() {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        Subscription subscription = subscriptionRepository.findByCollegeId(collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("No subscription found for this college"));
        return mapToResponse(subscription);
    }

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Page<SubscriptionResponse> getAllSubscriptions(Pageable pageable) {
        return subscriptionRepository.findAllSubscriptions(pageable)
                .map(this::mapToResponse);
    }

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Page<SubscriptionResponse> getSubscriptionsByStatus(SubscriptionStatus status, Pageable pageable) {
        return subscriptionRepository.findByStatus(status, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Optional<Subscription> getSubscriptionByCollegeId(Long collegeId) {
        return collegeId == null ? Optional.empty() : subscriptionRepository.findByCollegeId(collegeId);
    }

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Page<SubscriptionResponse> getSubscriptionsByCollegeId(Long collegeId, Pageable pageable) {
        List<Subscription> subscriptions = subscriptionRepository.findAllByCollegeId(collegeId);
        // Convert to page
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), subscriptions.size());
        List<Subscription> pagedSubscriptions = subscriptions.subList(start, end);

        List<SubscriptionResponse> responses = pagedSubscriptions.stream()
                .map(this::mapToResponse)
                .toList();

        return new org.springframework.data.domain.PageImpl<>(
                responses,
                pageable,
                subscriptions.size()
        );
    }

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Page<SubscriptionResponse> getExpiredSubscriptions(Pageable pageable) {
        List<Subscription> expiredSubscriptions = subscriptionRepository.findExpiredByCollegeId(
                tenantAccessGuard.getCurrentTenantId(), LocalDate.now());
        // Convert to page
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), expiredSubscriptions.size());
        List<Subscription> pagedSubscriptions = expiredSubscriptions.subList(start, end);

        List<SubscriptionResponse> responses = pagedSubscriptions.stream()
                .map(this::mapToResponse)
                .toList();

        return new org.springframework.data.domain.PageImpl<>(
                responses,
                pageable,
                expiredSubscriptions.size()
        );
    }

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Page<SubscriptionResponse> getSubscriptionsExpiringSoon(int days, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        LocalDate currentDate = LocalDate.now();
        LocalDate expiryDate = currentDate.plusDays(days);

        List<Subscription> expiringSubscriptions = subscriptionRepository.findExpiringSoonByCollegeId(
                collegeId, currentDate, expiryDate);
        // Convert to page
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), expiringSubscriptions.size());
        List<Subscription> pagedSubscriptions = expiringSubscriptions.subList(start, end);

        List<SubscriptionResponse> responses = pagedSubscriptions.stream()
                .map(this::mapToResponse)
                .toList();

        return new org.springframework.data.domain.PageImpl<>(
                responses,
                pageable,
                expiringSubscriptions.size()
        );
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public boolean isSubscriptionActive(String subscriptionUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Subscription subscription = subscriptionRepository.findByUuidAndCollegeId(subscriptionUuid, collegeId)
                .orElse(null);

        return subscription != null && subscription.isActive();
    }

    /* ============================================================
       INTERNAL HELPERS
       ============================================================ */


    private LocalDate calculateExpiryDate(LocalDate startDate, BillingCycle billingCycle) {
        return switch (billingCycle) {
            case MONTHLY -> startDate.plusMonths(1);
            case QUARTERLY -> startDate.plusMonths(3);
            case YEARLY -> startDate.plusYears(1);
        };
    }

    private void logSubscriptionHistory(
            Subscription subscription,
            SubscriptionStatus previousStatus,
            SubscriptionStatus newStatus,
            String reason,
            String changedBy
    ) {
        try {
            subscriptionHistoryRepository.save(
                    org.collegemanagement.entity.subscription.SubscriptionHistory.builder()
                            .subscription(subscription)
                            .previousStatus(previousStatus)
                            .newStatus(newStatus)
                            .changeReason(reason)
                            .changedBy(changedBy != null ? changedBy : "SYSTEM")
                            .build()
            );
        } catch (Exception e) {
            log.warn("Failed to log subscription history: {}", e.getMessage());
        }
    }

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof org.collegemanagement.entity.user.User user) {
            return user.getEmail();
        }
        return "SYSTEM";
    }

    private Subscription getSubscriptionForTenant(String subscriptionUuid) {

        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        return subscriptionRepository
                .findByUuidAndCollegeId(subscriptionUuid, collegeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Subscription not found with UUID: " + subscriptionUuid
                        )
                );
    }


    private SubscriptionResponse mapToResponse(Subscription subscription) {

        long invoiceCount = invoiceRepository
                .findBySubscriptionIdAndCollegeId(
                        subscription.getId(),
                        subscription.getCollege().getId()
                ).size();

        boolean isExpired = subscription.isExpired();

        return SubscriptionResponse.builder()
                .uuid(subscription.getUuid())
                .planType(subscription.getPlan().getCode())
                .billingCycle(subscription.getPlan().getBillingCycle())
                .price(subscription.getPlan().getPrice())
                .currency(subscription.getPlan().getCurrency())
                .status(subscription.getStatus())
                .startsAt(subscription.getStartsAt())
                .expiresAt(subscription.getExpiresAt())
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .collegeId(subscription.getCollege().getId())
                .collegeName(subscription.getCollege().getName())
                .invoiceCount(invoiceCount)
                .isActive(subscription.isActive())
                .isExpired(isExpired)
                .daysRemaining(
                        isExpired ? 0 :
                                ChronoUnit.DAYS.between(LocalDate.now(), subscription.getExpiresAt())
                )
                .build();
    }
}

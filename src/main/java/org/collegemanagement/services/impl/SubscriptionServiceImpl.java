package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.subscription.CreateSubscriptionRequest;
import org.collegemanagement.dto.subscription.RenewSubscriptionRequest;
import org.collegemanagement.dto.subscription.SubscriptionResponse;
import org.collegemanagement.dto.subscription.UpdateSubscriptionRequest;
import org.collegemanagement.entity.subscription.Subscription;
import org.collegemanagement.entity.subscription.SubscriptionPlan;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.enums.SubscriptionStatus;
import org.collegemanagement.exception.ResourceConflictException;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.repositories.CollegeRepository;
import org.collegemanagement.repositories.InvoiceRepository;
import org.collegemanagement.repositories.SubscriptionRepository;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.SubscriptionPlanService;
import org.collegemanagement.services.SubscriptionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanService subscriptionPlanService;
    private final CollegeRepository collegeRepository;
    private final InvoiceRepository invoiceRepository;
    private final TenantAccessGuard tenantAccessGuard;

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public SubscriptionResponse createSubscription(CreateSubscriptionRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Check if college already has an active subscription
        Subscription existingSubscription = subscriptionRepository.findByCollegeId(collegeId).orElse(null);
        if (existingSubscription != null && existingSubscription.isActive()) {
            throw new ResourceConflictException("College already has an active subscription");
        }

        // Get plan
        SubscriptionPlan plan = subscriptionPlanService.getActivePlan(request.getPlanType(), request.getBillingCycle());

        // Get college
        College college = collegeRepository.findById(collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("College not found"));

        // Calculate subscription dates
        LocalDate startsAt = LocalDate.now();
        LocalDate expiresAt = calculateExpiryDate(startsAt, request.getBillingCycle());

        // Create subscription
        Subscription subscription = Subscription.builder()
                .plan(plan)
                .college(college)
                .status(SubscriptionStatus.PENDING)
                .startsAt(startsAt)
                .expiresAt(expiresAt)
                .build();

        subscription = subscriptionRepository.save(subscription);

        return mapToResponse(subscription);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public SubscriptionResponse updateSubscription(String subscriptionUuid, UpdateSubscriptionRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Subscription subscription = subscriptionRepository.findByUuidAndCollegeId(subscriptionUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with UUID: " + subscriptionUuid));

        // Update plan if provided
        if (request.getPlanType() != null && request.getBillingCycle() != null) {
            SubscriptionPlan plan = subscriptionPlanService.getActivePlan(request.getPlanType(), request.getBillingCycle());
            subscription.setPlan(plan);
            
            // Recalculate expiry date if plan changed
            subscription.setExpiresAt(calculateExpiryDate(subscription.getStartsAt(), request.getBillingCycle()));
        }

        // Update status if provided
        if (request.getStatus() != null) {
            subscription.setStatus(request.getStatus());
        }

        subscription = subscriptionRepository.save(subscription);

        return mapToResponse(subscription);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public SubscriptionResponse cancelSubscription(String subscriptionUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Subscription subscription = subscriptionRepository.findByUuidAndCollegeId(subscriptionUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with UUID: " + subscriptionUuid));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription = subscriptionRepository.save(subscription);

        return mapToResponse(subscription);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public SubscriptionResponse renewSubscription(String subscriptionUuid, RenewSubscriptionRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Subscription existingSubscription = subscriptionRepository.findByUuidAndCollegeId(subscriptionUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with UUID: " + subscriptionUuid));

        // Get plan
        SubscriptionPlan plan = subscriptionPlanService.getActivePlan(request.getPlanType(), request.getBillingCycle());

        // Calculate new dates (start from current expiry or today, whichever is later)
        LocalDate startsAt = existingSubscription.getExpiresAt().isAfter(LocalDate.now()) 
                ? existingSubscription.getExpiresAt() 
                : LocalDate.now();
        LocalDate expiresAt = calculateExpiryDate(startsAt, request.getBillingCycle());

        // Update subscription
        existingSubscription.setPlan(plan);
        existingSubscription.setStatus(SubscriptionStatus.PENDING);
        existingSubscription.setStartsAt(startsAt);
        existingSubscription.setExpiresAt(expiresAt);

        existingSubscription = subscriptionRepository.save(existingSubscription);

        return mapToResponse(existingSubscription);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public SubscriptionResponse getSubscriptionByUuid(String subscriptionUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Subscription subscription = subscriptionRepository.findByUuidAndCollegeId(subscriptionUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with UUID: " + subscriptionUuid));

        return mapToResponse(subscription);
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
    public SubscriptionResponse activateSubscription(String subscriptionUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Subscription subscription = subscriptionRepository.findByUuidAndCollegeId(subscriptionUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with UUID: " + subscriptionUuid));

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription = subscriptionRepository.save(subscription);

        return mapToResponse(subscription);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public boolean isSubscriptionActive(String subscriptionUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Subscription subscription = subscriptionRepository.findByUuidAndCollegeId(subscriptionUuid, collegeId)
                .orElse(null);

        return subscription != null && subscription.isActive();
    }

    @Override
    public java.util.Optional<Subscription> getSubscriptionByCollegeId(Long collegeId) {
        if (collegeId == null) {
            return java.util.Optional.empty();
        }
        return subscriptionRepository.findByCollegeId(collegeId);
    }

    private LocalDate calculateExpiryDate(LocalDate startDate, org.collegemanagement.enums.BillingCycle billingCycle) {
        return switch (billingCycle) {
            case MONTHLY -> startDate.plusMonths(1);
            case QUARTERLY -> startDate.plusMonths(3);
            case YEARLY -> startDate.plusYears(1);
        };
    }

    private SubscriptionResponse mapToResponse(Subscription subscription) {
        long invoiceCount = invoiceRepository.findBySubscriptionIdAndCollegeId(
                subscription.getId(), subscription.getCollege().getId()).size();

        boolean isActive = subscription.isActive();
        boolean isExpired = subscription.isExpired();
        long daysRemaining = isExpired ? 0 : 
                java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), subscription.getExpiresAt());

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
                .isActive(isActive)
                .isExpired(isExpired)
                .daysRemaining(daysRemaining)
                .build();
    }
}

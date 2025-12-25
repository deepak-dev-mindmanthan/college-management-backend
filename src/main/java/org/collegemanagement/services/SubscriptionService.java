package org.collegemanagement.services;

import org.collegemanagement.dto.subscription.CreateSubscriptionRequest;
import org.collegemanagement.dto.subscription.RenewSubscriptionRequest;
import org.collegemanagement.dto.subscription.SubscriptionResponse;
import org.collegemanagement.dto.subscription.UpdateSubscriptionRequest;
import org.collegemanagement.enums.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SubscriptionService {

    /**
     * Create a new subscription
     */
    SubscriptionResponse createSubscription(CreateSubscriptionRequest request);

    /**
     * Update subscription
     */
    SubscriptionResponse updateSubscription(String subscriptionUuid, UpdateSubscriptionRequest request);

    /**
     * Cancel subscription
     */
    SubscriptionResponse cancelSubscription(String subscriptionUuid);

    /**
     * Renew subscription
     */
    SubscriptionResponse renewSubscription(String subscriptionUuid, RenewSubscriptionRequest request);

    /**
     * Get subscription by UUID
     */
    SubscriptionResponse getSubscriptionByUuid(String subscriptionUuid);

    /**
     * Get current subscription for college
     */
    SubscriptionResponse getCurrentSubscription();

    /**
     * Get all subscriptions with pagination (for super admin)
     */
    Page<SubscriptionResponse> getAllSubscriptions(Pageable pageable);

    /**
     * Get subscriptions by status (for super admin)
     */
    Page<SubscriptionResponse> getSubscriptionsByStatus(SubscriptionStatus status, Pageable pageable);

    /**
     * Get subscriptions by college ID (for super admin)
     */
    Page<SubscriptionResponse> getSubscriptionsByCollegeId(Long collegeId, Pageable pageable);

    /**
     * Get expired subscriptions
     */
    Page<SubscriptionResponse> getExpiredSubscriptions(Pageable pageable);

    /**
     * Get subscriptions expiring soon
     */
    Page<SubscriptionResponse> getSubscriptionsExpiringSoon(int days, Pageable pageable);

    /**
     * Activate subscription (after payment)
     */
    SubscriptionResponse activateSubscription(String subscriptionUuid);

    /**
     * Check if subscription is active
     */
    boolean isSubscriptionActive(String subscriptionUuid);

    /**
     * Get subscription by college ID
     * Returns Optional.empty() if no subscription exists
     */
    java.util.Optional<org.collegemanagement.entity.subscription.Subscription> getSubscriptionByCollegeId(Long collegeId);
}

package org.collegemanagement.security.tenant;

import org.collegemanagement.entity.subscription.Subscription;
import org.collegemanagement.security.errors.SecurityErrorCode;
import org.collegemanagement.services.SubscriptionService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionAccessGuard {

    private final SubscriptionService subscriptionService;

    public SubscriptionAccessGuard(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    public void assertActiveSubscription(Long collegeId) {
        boolean active = subscriptionService
                .getSubscriptionByCollegeId(collegeId)
                .map(Subscription::isActive)
                .orElse(false);

        if (!active) {
            throw new AccessDeniedException(
                    SecurityErrorCode.SUBSCRIPTION_EXPIRED.name()
            );
        }
    }
}


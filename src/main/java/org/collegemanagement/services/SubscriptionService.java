package org.collegemanagement.services;

import org.collegemanagement.dto.SubscriptionRequest;
import org.collegemanagement.entity.College;
import org.collegemanagement.entity.Subscription;
import org.collegemanagement.entity.User;

public interface SubscriptionService {
    Subscription createOrUpdateForCollege(College college, SubscriptionRequest request);
    Subscription getActiveSubscription(Long collegeId);
    Subscription ensureActiveSubscription(User user);
}


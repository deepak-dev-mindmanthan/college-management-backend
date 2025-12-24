package org.collegemanagement.services;


import org.collegemanagement.dto.SubscriptionRequest;
import org.collegemanagement.dto.SubscriptionStatusResponse;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.subscription.Subscription;

import java.util.Optional;

public interface SubscriptionService {
    Subscription createSubscriptionForCollege(College college, SubscriptionRequest request);
    Optional<Subscription> getSubscriptionByCollegeId(Long collegeId);
    SubscriptionStatusResponse getStatus(College college);

}
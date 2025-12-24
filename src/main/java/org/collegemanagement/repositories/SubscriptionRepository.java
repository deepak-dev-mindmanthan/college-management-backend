package org.collegemanagement.repositories;

import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.subscription.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByCollegeId(Long collegeId);
    Optional<Subscription> findByCollege(College college);
}


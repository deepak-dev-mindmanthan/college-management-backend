package org.collegemanagement.repositories;

import org.collegemanagement.entity.Subscription;
import org.collegemanagement.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findTopByCollegeIdAndStatusInOrderByExpiresAtDesc(Long collegeId, List<SubscriptionStatus> statuses);
    List<Subscription> findByCollegeIdAndStatusIn(Long collegeId, List<SubscriptionStatus> statuses);
}


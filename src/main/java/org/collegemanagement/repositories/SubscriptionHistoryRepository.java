package org.collegemanagement.repositories;

import org.collegemanagement.entity.subscription.SubscriptionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, Long> {

    /**
     * Find all history records for a subscription
     */
    @Query("""
            SELECT h FROM SubscriptionHistory h
            WHERE h.subscription.id = :subscriptionId
            ORDER BY h.createdAt DESC
            """)
    List<SubscriptionHistory> findBySubscriptionId(@Param("subscriptionId") Long subscriptionId);

    /**
     * Find history records by subscription UUID
     */
    @Query("""
            SELECT h FROM SubscriptionHistory h
            WHERE h.subscription.uuid = :subscriptionUuid
            ORDER BY h.createdAt DESC
            """)
    List<SubscriptionHistory> findBySubscriptionUuid(@Param("subscriptionUuid") String subscriptionUuid);
}


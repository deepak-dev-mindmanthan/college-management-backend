package org.collegemanagement.repositories;


import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.subscription.Subscription;
import org.collegemanagement.enums.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    /**
     * Find subscription by college ID
     */
    Optional<Subscription> findByCollegeId(Long collegeId);
    
    /**
     * Find subscription by college
     */
    Optional<Subscription> findByCollege(College college);

    /**
     * Find subscription by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT s FROM Subscription s
            WHERE s.uuid = :uuid
            AND s.college.id = :collegeId
            """)
    Optional<Subscription> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find all subscriptions by college ID
     */
    @Query("""
            SELECT s FROM Subscription s
            WHERE s.college.id = :collegeId
            ORDER BY s.createdAt DESC
            """)
    List<Subscription> findAllByCollegeId(@Param("collegeId") Long collegeId);

    /**
     * Find subscriptions by status and college ID
     */
    @Query("""
            SELECT s FROM Subscription s
            WHERE s.status = :status
            AND s.college.id = :collegeId
            ORDER BY s.createdAt DESC
            """)
    Page<Subscription> findByStatusAndCollegeId(@Param("status") SubscriptionStatus status, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find active subscriptions by college ID
     */
    @Query("""
            SELECT s FROM Subscription s
            WHERE s.college.id = :collegeId
            AND s.status = 'ACTIVE'
            AND s.expiresAt >= :currentDate
            """)
    Optional<Subscription> findActiveByCollegeId(@Param("collegeId") Long collegeId, @Param("currentDate") LocalDate currentDate);

    /**
     * Find expired subscriptions by college ID
     */
    @Query("""
            SELECT s FROM Subscription s
            WHERE s.college.id = :collegeId
            AND s.expiresAt < :currentDate
            ORDER BY s.expiresAt DESC
            """)
    List<Subscription> findExpiredByCollegeId(@Param("collegeId") Long collegeId, @Param("currentDate") LocalDate currentDate);

    /**
     * Find subscriptions expiring soon (within days) by college ID
     */
    @Query("""
            SELECT s FROM Subscription s
            WHERE s.college.id = :collegeId
            AND s.status = 'ACTIVE'
            AND s.expiresAt BETWEEN :currentDate AND :expiryDate
            ORDER BY s.expiresAt ASC
            """)
    List<Subscription> findExpiringSoonByCollegeId(
            @Param("collegeId") Long collegeId,
            @Param("currentDate") LocalDate currentDate,
            @Param("expiryDate") LocalDate expiryDate
    );

    /**
     * Count subscriptions by status and college ID
     */
    @Query("""
            SELECT COUNT(s) FROM Subscription s
            WHERE s.status = :status
            AND s.college.id = :collegeId
            """)
    long countByStatusAndCollegeId(@Param("status") SubscriptionStatus status, @Param("collegeId") Long collegeId);

    /**
     * Find all subscriptions (for super admin)
     */
    @Query("""
            SELECT s FROM Subscription s
            ORDER BY s.createdAt DESC
            """)
    Page<Subscription> findAllSubscriptions(Pageable pageable);

    /**
     * Find subscriptions expiring soon across all colleges (for scheduled jobs)
     */
    @Query("""
            SELECT s FROM Subscription s
            WHERE s.status = 'ACTIVE'
            AND s.expiresAt BETWEEN :currentDate AND :expiryDate
            ORDER BY s.expiresAt ASC
            """)
    List<Subscription> findExpiringSoon(@Param("currentDate") LocalDate currentDate, @Param("expiryDate") LocalDate expiryDate);

    /**
     * Find subscriptions by status (for super admin)
     */
    @Query("""
            SELECT s FROM Subscription s
            WHERE s.status = :status
            ORDER BY s.createdAt DESC
            """)
    Page<Subscription> findByStatus(@Param("status") SubscriptionStatus status, Pageable pageable);
}

package org.collegemanagement.repositories;

import org.collegemanagement.entity.communication.Notification;
import org.collegemanagement.enums.NotificationReferenceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find notifications by user ID and college ID (college isolation)
     */
    @Query("""
            SELECT n FROM Notification n
            JOIN n.user u
            WHERE n.user.id = :userId
            AND u.college.id = :collegeId
            ORDER BY n.createdAt DESC
            """)
    Page<Notification> findByUserIdAndCollegeId(@Param("userId") Long userId, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find unread notifications by user ID and college ID
     */
    @Query("""
            SELECT n FROM Notification n
            JOIN n.user u
            WHERE n.user.id = :userId
            AND u.college.id = :collegeId
            AND n.isRead = false
            ORDER BY n.createdAt DESC
            """)
    List<Notification> findUnreadByUserIdAndCollegeId(@Param("userId") Long userId, @Param("collegeId") Long collegeId);

    /**
     * Find notifications by reference type and reference ID
     */
    @Query("""
            SELECT n FROM Notification n
            JOIN n.user u
            WHERE n.referenceType = :referenceType
            AND n.referenceId = :referenceId
            AND u.college.id = :collegeId
            """)
    List<Notification> findByReferenceTypeAndReferenceIdAndCollegeId(
            @Param("referenceType") NotificationReferenceType referenceType,
            @Param("referenceId") Long referenceId,
            @Param("collegeId") Long collegeId);

    /**
     * Count unread notifications by user ID and college ID
     */
    @Query("""
            SELECT COUNT(n) FROM Notification n
            JOIN n.user u
            WHERE n.user.id = :userId
            AND u.college.id = :collegeId
            AND n.isRead = false
            """)
    long countUnreadByUserIdAndCollegeId(@Param("userId") Long userId, @Param("collegeId") Long collegeId);
}


package org.collegemanagement.repositories;

import org.collegemanagement.entity.audit.AuditLog;
import org.collegemanagement.enums.AuditAction;
import org.collegemanagement.enums.AuditEntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find audit logs by college ID with pagination
     */
    @Query("""
            SELECT a FROM AuditLog a
            WHERE a.college.id = :collegeId
            ORDER BY a.createdAt DESC
            """)
    Page<AuditLog> findByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find audit logs by entity type and entity ID
     */
    @Query("""
            SELECT a FROM AuditLog a
            WHERE a.entityType = :entityType
            AND a.entityId = :entityId
            AND a.college.id = :collegeId
            ORDER BY a.createdAt DESC
            """)
    Page<AuditLog> findByEntityTypeAndEntityIdAndCollegeId(
            @Param("entityType") AuditEntityType entityType,
            @Param("entityId") Long entityId,
            @Param("collegeId") Long collegeId,
            Pageable pageable);

    /**
     * Find audit logs by user ID and college ID
     */
    @Query("""
            SELECT a FROM AuditLog a
            WHERE a.user.id = :userId
            AND a.college.id = :collegeId
            ORDER BY a.createdAt DESC
            """)
    Page<AuditLog> findByUserIdAndCollegeId(@Param("userId") Long userId, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find audit logs by action and college ID
     */
    @Query("""
            SELECT a FROM AuditLog a
            WHERE a.action = :action
            AND a.college.id = :collegeId
            ORDER BY a.createdAt DESC
            """)
    Page<AuditLog> findByActionAndCollegeId(@Param("action") AuditAction action, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find audit logs by date range and college ID
     */
    @Query("""
            SELECT a FROM AuditLog a
            WHERE a.college.id = :collegeId
            AND a.createdAt >= :startDate
            AND a.createdAt <= :endDate
            ORDER BY a.createdAt DESC
            """)
    Page<AuditLog> findByDateRangeAndCollegeId(
            @Param("collegeId") Long collegeId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable);
}


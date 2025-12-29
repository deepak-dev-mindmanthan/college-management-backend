package org.collegemanagement.repositories;

import org.collegemanagement.entity.leave.LeaveRequest;
import org.collegemanagement.enums.LeaveOwnerType;
import org.collegemanagement.enums.LeaveStatus;
import org.collegemanagement.enums.LeaveType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    /**
     * Find leave request by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT lr FROM LeaveRequest lr
            WHERE lr.uuid = :uuid
            AND lr.user.college.id = :collegeId
            """)
    Optional<LeaveRequest> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find all leave requests by college ID with pagination
     */
    @Query("""
            SELECT lr FROM LeaveRequest lr
            WHERE lr.user.college.id = :collegeId
            ORDER BY lr.startDate DESC, lr.createdAt DESC
            """)
    Page<LeaveRequest> findAllByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find leave requests by user UUID and college ID
     */
    @Query("""
            SELECT lr FROM LeaveRequest lr
            WHERE lr.user.uuid = :userUuid
            AND lr.user.college.id = :collegeId
            ORDER BY lr.startDate DESC
            """)
    Page<LeaveRequest> findByUserUuidAndCollegeId(
            @Param("userUuid") String userUuid,
            @Param("collegeId") Long collegeId,
            Pageable pageable
    );

    /**
     * Find leave requests by owner type and college ID
     */
    @Query("""
            SELECT lr FROM LeaveRequest lr
            WHERE lr.ownerType = :ownerType
            AND lr.user.college.id = :collegeId
            ORDER BY lr.startDate DESC
            """)
    Page<LeaveRequest> findByOwnerTypeAndCollegeId(
            @Param("ownerType") LeaveOwnerType ownerType,
            @Param("collegeId") Long collegeId,
            Pageable pageable
    );

    /**
     * Find leave requests by status and college ID
     */
    @Query("""
            SELECT lr FROM LeaveRequest lr
            WHERE lr.status = :status
            AND lr.user.college.id = :collegeId
            ORDER BY lr.startDate DESC
            """)
    Page<LeaveRequest> findByStatusAndCollegeId(
            @Param("status") LeaveStatus status,
            @Param("collegeId") Long collegeId,
            Pageable pageable
    );

    /**
     * Find leave requests by user UUID, status, and college ID
     */
    @Query("""
            SELECT lr FROM LeaveRequest lr
            WHERE lr.user.uuid = :userUuid
            AND lr.status = :status
            AND lr.user.college.id = :collegeId
            ORDER BY lr.startDate DESC
            """)
    Page<LeaveRequest> findByUserUuidAndStatusAndCollegeId(
            @Param("userUuid") String userUuid,
            @Param("status") LeaveStatus status,
            @Param("collegeId") Long collegeId,
            Pageable pageable
    );

    /**
     * Find leave requests by date range and college ID
     */
    @Query("""
            SELECT lr FROM LeaveRequest lr
            WHERE lr.user.college.id = :collegeId
            AND lr.startDate <= :endDate
            AND lr.endDate >= :startDate
            ORDER BY lr.startDate DESC
            """)
    Page<LeaveRequest> findByDateRangeAndCollegeId(
            @Param("collegeId") Long collegeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    /**
     * Find leave requests by leave type and college ID
     */
    @Query("""
            SELECT lr FROM LeaveRequest lr
            WHERE lr.leaveType = :leaveType
            AND lr.user.college.id = :collegeId
            ORDER BY lr.startDate DESC
            """)
    Page<LeaveRequest> findByLeaveTypeAndCollegeId(
            @Param("leaveType") LeaveType leaveType,
            @Param("collegeId") Long collegeId,
            Pageable pageable
    );

    /**
     * Find overlapping leave requests for a user (for validation)
     */
    @Query("""
            SELECT lr FROM LeaveRequest lr
            WHERE lr.user.id = :userId
            AND lr.status IN :statuses
            AND lr.startDate <= :endDate
            AND lr.endDate >= :startDate
            AND lr.id != :excludeId
            """)
    List<LeaveRequest> findOverlappingLeaves(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") List<LeaveStatus> statuses,
            @Param("excludeId") Long excludeId
    );

    /**
     * Count leave requests by status and college ID
     */
    @Query("""
            SELECT COUNT(lr) FROM LeaveRequest lr
            WHERE lr.status = :status
            AND lr.user.college.id = :collegeId
            """)
    long countByStatusAndCollegeId(@Param("status") LeaveStatus status, @Param("collegeId") Long collegeId);

    /**
     * Count leave requests by user UUID and college ID
     */
    @Query("""
            SELECT COUNT(lr) FROM LeaveRequest lr
            WHERE lr.user.uuid = :userUuid
            AND lr.user.college.id = :collegeId
            """)
    long countByUserUuidAndCollegeId(@Param("userUuid") String userUuid, @Param("collegeId") Long collegeId);
}


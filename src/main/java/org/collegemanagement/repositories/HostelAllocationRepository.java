package org.collegemanagement.repositories;

import org.collegemanagement.entity.hostel.HostelAllocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HostelAllocationRepository extends JpaRepository<HostelAllocation, Long> {

    /**
     * Find hostel allocation by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT ha FROM HostelAllocation ha
            WHERE ha.uuid = :uuid
            AND ha.student.college.id = :collegeId
            """)
    Optional<HostelAllocation> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find all hostel allocations by college ID with pagination
     */
    @Query("""
            SELECT ha FROM HostelAllocation ha
            WHERE ha.student.college.id = :collegeId
            ORDER BY ha.allocatedAt DESC
            """)
    Page<HostelAllocation> findAllByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find all active hostel allocations by college ID (releasedAt is null)
     */
    @Query("""
            SELECT ha FROM HostelAllocation ha
            WHERE ha.student.college.id = :collegeId
            AND ha.releasedAt IS NULL
            ORDER BY ha.allocatedAt DESC
            """)
    Page<HostelAllocation> findActiveByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find all hostel allocations by student UUID and college ID
     */
    @Query("""
            SELECT ha FROM HostelAllocation ha
            WHERE ha.student.uuid = :studentUuid
            AND ha.student.college.id = :collegeId
            ORDER BY ha.allocatedAt DESC
            """)
    List<HostelAllocation> findByStudentUuidAndCollegeId(@Param("studentUuid") String studentUuid, @Param("collegeId") Long collegeId);

    /**
     * Find active hostel allocation by student UUID and college ID
     */
    @Query("""
            SELECT ha FROM HostelAllocation ha
            WHERE ha.student.uuid = :studentUuid
            AND ha.student.college.id = :collegeId
            AND ha.releasedAt IS NULL
            """)
    Optional<HostelAllocation> findActiveByStudentUuidAndCollegeId(@Param("studentUuid") String studentUuid, @Param("collegeId") Long collegeId);

    /**
     * Find all hostel allocations by room UUID and college ID with pagination
     */
    @Query("""
            SELECT ha FROM HostelAllocation ha
            WHERE ha.room.uuid = :roomUuid
            AND ha.student.college.id = :collegeId
            ORDER BY ha.allocatedAt DESC
            """)
    Page<HostelAllocation> findByRoomUuidAndCollegeId(@Param("roomUuid") String roomUuid, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find active hostel allocations by room UUID and college ID
     */
    @Query("""
            SELECT ha FROM HostelAllocation ha
            WHERE ha.room.uuid = :roomUuid
            AND ha.student.college.id = :collegeId
            AND ha.releasedAt IS NULL
            ORDER BY ha.allocatedAt DESC
            """)
    List<HostelAllocation> findActiveByRoomUuidAndCollegeId(@Param("roomUuid") String roomUuid, @Param("collegeId") Long collegeId);

    /**
     * Find all hostel allocations by hostel UUID and college ID with pagination
     */
    @Query("""
            SELECT ha FROM HostelAllocation ha
            WHERE ha.room.hostel.uuid = :hostelUuid
            AND ha.student.college.id = :collegeId
            ORDER BY ha.allocatedAt DESC
            """)
    Page<HostelAllocation> findByHostelUuidAndCollegeId(@Param("hostelUuid") String hostelUuid, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find active hostel allocations by hostel UUID and college ID
     */
    @Query("""
            SELECT ha FROM HostelAllocation ha
            WHERE ha.room.hostel.uuid = :hostelUuid
            AND ha.student.college.id = :collegeId
            AND ha.releasedAt IS NULL
            ORDER BY ha.allocatedAt DESC
            """)
    List<HostelAllocation> findActiveByHostelUuidAndCollegeId(@Param("hostelUuid") String hostelUuid, @Param("collegeId") Long collegeId);

    /**
     * Check if student has active allocation
     */
    @Query("""
            SELECT COUNT(ha) > 0 FROM HostelAllocation ha
            WHERE ha.student.uuid = :studentUuid
            AND ha.student.college.id = :collegeId
            AND ha.releasedAt IS NULL
            """)
    boolean existsActiveByStudentUuidAndCollegeId(@Param("studentUuid") String studentUuid, @Param("collegeId") Long collegeId);

    /**
     * Count active hostel allocations by room UUID and college ID
     */
    @Query("""
            SELECT COUNT(ha) FROM HostelAllocation ha
            WHERE ha.room.uuid = :roomUuid
            AND ha.student.college.id = :collegeId
            AND ha.releasedAt IS NULL
            """)
    long countActiveByRoomUuidAndCollegeId(@Param("roomUuid") String roomUuid, @Param("collegeId") Long collegeId);

    /**
     * Count active hostel allocations by college ID
     */
    @Query("""
            SELECT COUNT(ha) FROM HostelAllocation ha
            WHERE ha.student.college.id = :collegeId
            AND ha.releasedAt IS NULL
            """)
    long countActiveByCollegeId(@Param("collegeId") Long collegeId);

    /**
     * Search hostel allocations by student name, roll number, room number, or hostel name
     */
    @Query("""
            SELECT ha FROM HostelAllocation ha
            WHERE ha.student.college.id = :collegeId
            AND (LOWER(ha.student.user.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(ha.student.rollNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(ha.room.roomNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(ha.room.hostel.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
            ORDER BY ha.allocatedAt DESC
            """)
    Page<HostelAllocation> searchByCollegeId(@Param("collegeId") Long collegeId, @Param("searchTerm") String searchTerm, Pageable pageable);
}


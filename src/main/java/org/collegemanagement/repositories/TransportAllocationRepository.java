package org.collegemanagement.repositories;

import org.collegemanagement.entity.transport.TransportAllocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransportAllocationRepository extends JpaRepository<TransportAllocation, Long> {

    /**
     * Find transport allocation by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT ta FROM TransportAllocation ta
            WHERE ta.uuid = :uuid
            AND ta.student.college.id = :collegeId
            """)
    Optional<TransportAllocation> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find all transport allocations by college ID with pagination
     */
    @Query("""
            SELECT ta FROM TransportAllocation ta
            WHERE ta.student.college.id = :collegeId
            ORDER BY ta.allocatedAt DESC
            """)
    Page<TransportAllocation> findAllByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find all active transport allocations by college ID (releasedAt is null)
     */
    @Query("""
            SELECT ta FROM TransportAllocation ta
            WHERE ta.student.college.id = :collegeId
            AND ta.releasedAt IS NULL
            ORDER BY ta.allocatedAt DESC
            """)
    Page<TransportAllocation> findActiveByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find all transport allocations by student UUID and college ID
     */
    @Query("""
            SELECT ta FROM TransportAllocation ta
            WHERE ta.student.uuid = :studentUuid
            AND ta.student.college.id = :collegeId
            ORDER BY ta.allocatedAt DESC
            """)
    List<TransportAllocation> findByStudentUuidAndCollegeId(@Param("studentUuid") String studentUuid, @Param("collegeId") Long collegeId);

    /**
     * Find active transport allocation by student UUID and college ID
     */
    @Query("""
            SELECT ta FROM TransportAllocation ta
            WHERE ta.student.uuid = :studentUuid
            AND ta.student.college.id = :collegeId
            AND ta.releasedAt IS NULL
            """)
    Optional<TransportAllocation> findActiveByStudentUuidAndCollegeId(@Param("studentUuid") String studentUuid, @Param("collegeId") Long collegeId);

    /**
     * Find all transport allocations by route UUID and college ID with pagination
     */
    @Query("""
            SELECT ta FROM TransportAllocation ta
            WHERE ta.route.uuid = :routeUuid
            AND ta.student.college.id = :collegeId
            ORDER BY ta.allocatedAt DESC
            """)
    Page<TransportAllocation> findByRouteUuidAndCollegeId(@Param("routeUuid") String routeUuid, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find active transport allocations by route UUID and college ID
     */
    @Query("""
            SELECT ta FROM TransportAllocation ta
            WHERE ta.route.uuid = :routeUuid
            AND ta.student.college.id = :collegeId
            AND ta.releasedAt IS NULL
            ORDER BY ta.allocatedAt DESC
            """)
    List<TransportAllocation> findActiveByRouteUuidAndCollegeId(@Param("routeUuid") String routeUuid, @Param("collegeId") Long collegeId);

    /**
     * Check if student has active allocation for a route
     */
    @Query("""
            SELECT COUNT(ta) > 0 FROM TransportAllocation ta
            WHERE ta.student.uuid = :studentUuid
            AND ta.route.uuid = :routeUuid
            AND ta.student.college.id = :collegeId
            AND ta.releasedAt IS NULL
            """)
    boolean existsActiveByStudentUuidAndRouteUuidAndCollegeId(
            @Param("studentUuid") String studentUuid,
            @Param("routeUuid") String routeUuid,
            @Param("collegeId") Long collegeId
    );

    /**
     * Count active transport allocations by college ID
     */
    @Query("""
            SELECT COUNT(ta) FROM TransportAllocation ta
            WHERE ta.student.college.id = :collegeId
            AND ta.releasedAt IS NULL
            """)
    long countActiveByCollegeId(@Param("collegeId") Long collegeId);

    /**
     * Count transport allocations by route UUID and college ID
     */
    @Query("""
            SELECT COUNT(ta) FROM TransportAllocation ta
            WHERE ta.route.uuid = :routeUuid
            AND ta.student.college.id = :collegeId
            """)
    long countByRouteUuidAndCollegeId(@Param("routeUuid") String routeUuid, @Param("collegeId") Long collegeId);

    /**
     * Search transport allocations by student name, roll number, route name, or vehicle number
     */
    @Query("""
            SELECT ta FROM TransportAllocation ta
            WHERE ta.student.college.id = :collegeId
            AND (LOWER(ta.student.user.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(ta.student.rollNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(ta.route.routeName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(ta.route.vehicleNo) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
            ORDER BY ta.allocatedAt DESC
            """)
    Page<TransportAllocation> searchByCollegeId(@Param("collegeId") Long collegeId, @Param("searchTerm") String searchTerm, Pageable pageable);
}


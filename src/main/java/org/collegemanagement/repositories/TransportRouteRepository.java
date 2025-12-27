package org.collegemanagement.repositories;

import org.collegemanagement.entity.transport.TransportRoute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TransportRouteRepository extends JpaRepository<TransportRoute, Long> {

    /**
     * Find transport route by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT tr FROM TransportRoute tr
            WHERE tr.uuid = :uuid
            AND tr.college.id = :collegeId
            """)
    Optional<TransportRoute> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find all transport routes by college ID with pagination
     */
    @Query("""
            SELECT tr FROM TransportRoute tr
            WHERE tr.college.id = :collegeId
            ORDER BY tr.routeName ASC
            """)
    Page<TransportRoute> findAllByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Check if route name exists for a college
     */
    @Query("""
            SELECT COUNT(tr) > 0 FROM TransportRoute tr
            WHERE tr.routeName = :routeName
            AND tr.college.id = :collegeId
            """)
    boolean existsByRouteNameAndCollegeId(@Param("routeName") String routeName, @Param("collegeId") Long collegeId);

    /**
     * Check if route name exists for a college excluding a specific route (for updates)
     */
    @Query("""
            SELECT COUNT(tr) > 0 FROM TransportRoute tr
            WHERE tr.routeName = :routeName
            AND tr.college.id = :collegeId
            AND tr.id != :excludeId
            """)
    boolean existsByRouteNameAndCollegeIdAndIdNot(@Param("routeName") String routeName, @Param("collegeId") Long collegeId, @Param("excludeId") Long excludeId);

    /**
     * Search transport routes by route name or vehicle number within a college
     */
    @Query("""
            SELECT tr FROM TransportRoute tr
            WHERE tr.college.id = :collegeId
            AND (LOWER(tr.routeName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(tr.vehicleNo) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(tr.driverName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
            ORDER BY tr.routeName ASC
            """)
    Page<TransportRoute> searchByCollegeId(@Param("collegeId") Long collegeId, @Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Count transport routes by college ID
     */
    @Query("""
            SELECT COUNT(tr) FROM TransportRoute tr
            WHERE tr.college.id = :collegeId
            """)
    long countByCollegeId(@Param("collegeId") Long collegeId);
}


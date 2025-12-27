package org.collegemanagement.repositories;

import org.collegemanagement.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface HostelManagerRepository extends JpaRepository<User, Long> {

    /**
     * Find all hostel managers by college ID with role filtering
     */
    @Query("""
            SELECT u FROM User u
            JOIN u.roles r
            WHERE u.college.id = :collegeId
            AND r.name = 'ROLE_HOSTEL_MANAGER'
            ORDER BY u.name ASC
            """)
    Page<User> findAllHostelManagersByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find hostel manager by ID and college ID with role verification
     */
    @Query("""
            SELECT u FROM User u
            JOIN u.roles r
            WHERE u.id = :hostelManagerId
            AND u.college.id = :collegeId
            AND r.name = 'ROLE_HOSTEL_MANAGER'
            """)
    Optional<User> findHostelManagerByIdAndCollegeId(@Param("hostelManagerId") Long hostelManagerId, @Param("collegeId") Long collegeId);

    /**
     * Find hostel manager by UUID and college ID with role verification
     */
    @Query("""
            SELECT u FROM User u
            JOIN u.roles r
            WHERE u.uuid = :uuid
            AND u.college.id = :collegeId
            AND r.name = 'ROLE_HOSTEL_MANAGER'
            """)
    Optional<User> findHostelManagerByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Check if hostel manager exists by email and college ID
     */
    @Query("""
            SELECT COUNT(u) > 0 FROM User u
            JOIN u.roles r
            WHERE u.email = :email
            AND u.college.id = :collegeId
            AND r.name = 'ROLE_HOSTEL_MANAGER'
            """)
    boolean existsByEmailAndCollegeId(@Param("email") String email, @Param("collegeId") Long collegeId);

    /**
     * Check if hostel manager exists by email, college ID, and not the given ID (for updates)
     */
    @Query("""
            SELECT COUNT(u) > 0 FROM User u
            JOIN u.roles r
            WHERE u.email = :email
            AND u.college.id = :collegeId
            AND u.id != :excludeId
            AND r.name = 'ROLE_HOSTEL_MANAGER'
            """)
    boolean existsByEmailAndCollegeIdAndIdNot(@Param("email") String email, @Param("collegeId") Long collegeId, @Param("excludeId") Long excludeId);

    /**
     * Search hostel managers by name or email within a college
     */
    @Query("""
            SELECT u FROM User u
            JOIN u.roles r
            WHERE u.college.id = :collegeId
            AND r.name = 'ROLE_HOSTEL_MANAGER'
            AND (LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
            ORDER BY u.name ASC
            """)
    Page<User> searchHostelManagersByCollegeId(@Param("collegeId") Long collegeId, @Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Count hostel managers by college ID
     */
    @Query("""
            SELECT COUNT(u) FROM User u
            JOIN u.roles r
            WHERE u.college.id = :collegeId
            AND r.name = 'ROLE_HOSTEL_MANAGER'
            """)
    long countByCollegeId(@Param("collegeId") Long collegeId);
}


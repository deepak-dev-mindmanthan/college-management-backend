package org.collegemanagement.repositories;

import org.collegemanagement.entity.hostel.Hostel;
import org.collegemanagement.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HostelWardenRepository extends JpaRepository<User, Long> {

    /**
     * Find all hostel wardens by college ID with role filtering
     */
    @Query("""
            SELECT u FROM User u
            JOIN u.roles r
            WHERE u.college.id = :collegeId
            AND r.name = 'ROLE_HOSTEL_WARDEN'
            ORDER BY u.name ASC
            """)
    Page<User> findAllHostelWardensByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find hostel warden by ID and college ID with role verification
     */
    @Query("""
            SELECT u FROM User u
            JOIN u.roles r
            WHERE u.id = :wardenId
            AND u.college.id = :collegeId
            AND r.name = 'ROLE_HOSTEL_WARDEN'
            """)
    Optional<User> findHostelWardenByIdAndCollegeId(@Param("wardenId") Long wardenId, @Param("collegeId") Long collegeId);

    /**
     * Find hostel warden by UUID and college ID with role verification
     */
    @Query("""
            SELECT u FROM User u
            JOIN u.roles r
            WHERE u.uuid = :uuid
            AND u.college.id = :collegeId
            AND r.name = 'ROLE_HOSTEL_WARDEN'
            """)
    Optional<User> findHostelWardenByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Check if hostel warden exists by email and college ID
     */
    @Query("""
            SELECT COUNT(u) > 0 FROM User u
            JOIN u.roles r
            WHERE u.email = :email
            AND u.college.id = :collegeId
            AND r.name = 'ROLE_HOSTEL_WARDEN'
            """)
    boolean existsByEmailAndCollegeId(@Param("email") String email, @Param("collegeId") Long collegeId);

    /**
     * Check if hostel warden exists by email, college ID, and not the given ID (for updates)
     */
    @Query("""
            SELECT COUNT(u) > 0 FROM User u
            JOIN u.roles r
            WHERE u.email = :email
            AND u.college.id = :collegeId
            AND u.id != :excludeId
            AND r.name = 'ROLE_HOSTEL_WARDEN'
            """)
    boolean existsByEmailAndCollegeIdAndIdNot(@Param("email") String email, @Param("collegeId") Long collegeId, @Param("excludeId") Long excludeId);

    /**
     * Search hostel wardens by name or email within a college
     */
    @Query("""
            SELECT u FROM User u
            JOIN u.roles r
            WHERE u.college.id = :collegeId
            AND r.name = 'ROLE_HOSTEL_WARDEN'
            AND (LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
            ORDER BY u.name ASC
            """)
    Page<User> searchHostelWardensByCollegeId(@Param("collegeId") Long collegeId, @Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Count hostel wardens by college ID
     */
    @Query("""
            SELECT COUNT(u) FROM User u
            JOIN u.roles r
            WHERE u.college.id = :collegeId
            AND r.name = 'ROLE_HOSTEL_WARDEN'
            """)
    long countByCollegeId(@Param("collegeId") Long collegeId);

    /**
     * Find all hostels assigned to a warden
     */
    @Query("""
            SELECT h.uuid FROM Hostel h
            WHERE h.warden.id = :wardenId
            AND h.college.id = :collegeId
            """)
    List<String> findAssignedHostelUuidsByWardenIdAndCollegeId(@Param("wardenId") Long wardenId, @Param("collegeId") Long collegeId);
}


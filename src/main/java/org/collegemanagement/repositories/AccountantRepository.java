package org.collegemanagement.repositories;

import org.collegemanagement.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountantRepository extends JpaRepository<User, Long> {

    /**
     * Find all accountants by college ID with role filtering
     */
    @Query("""
            SELECT u FROM User u
            JOIN u.roles r
            WHERE u.college.id = :collegeId
            AND r.name = 'ROLE_ACCOUNTANT'
            ORDER BY u.name ASC
            """)
    Page<User> findAllAccountantsByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find accountant by ID and college ID with role verification
     */
    @Query("""
            SELECT u FROM User u
            JOIN u.roles r
            WHERE u.id = :accountantId
            AND u.college.id = :collegeId
            AND r.name = 'ROLE_ACCOUNTANT'
            """)
    Optional<User> findAccountantByIdAndCollegeId(@Param("accountantId") Long accountantId, @Param("collegeId") Long collegeId);

    /**
     * Find accountant by UUID and college ID with role verification
     */
    @Query("""
            SELECT u FROM User u
            JOIN u.roles r
            WHERE u.uuid = :uuid
            AND u.college.id = :collegeId
            AND r.name = 'ROLE_ACCOUNTANT'
            """)
    Optional<User> findAccountantByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Check if accountant exists by email and college ID
     */
    @Query("""
            SELECT COUNT(u) > 0 FROM User u
            JOIN u.roles r
            WHERE u.email = :email
            AND u.college.id = :collegeId
            AND r.name = 'ROLE_ACCOUNTANT'
            """)
    boolean existsByEmailAndCollegeId(@Param("email") String email, @Param("collegeId") Long collegeId);

    /**
     * Check if accountant exists by email, college ID, and not the given ID (for updates)
     */
    @Query("""
            SELECT COUNT(u) > 0 FROM User u
            JOIN u.roles r
            WHERE u.email = :email
            AND u.college.id = :collegeId
            AND u.id != :excludeId
            AND r.name = 'ROLE_ACCOUNTANT'
            """)
    boolean existsByEmailAndCollegeIdAndIdNot(@Param("email") String email, @Param("collegeId") Long collegeId, @Param("excludeId") Long excludeId);

    /**
     * Search accountants by name or email within a college
     */
    @Query("""
            SELECT u FROM User u
            JOIN u.roles r
            WHERE u.college.id = :collegeId
            AND r.name = 'ROLE_ACCOUNTANT'
            AND (LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
            ORDER BY u.name ASC
            """)
    Page<User> searchAccountantsByCollegeId(@Param("collegeId") Long collegeId, @Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Count accountants by college ID
     */
    @Query("""
            SELECT COUNT(u) FROM User u
            JOIN u.roles r
            WHERE u.college.id = :collegeId
            AND r.name = 'ROLE_ACCOUNTANT'
            """)
    long countByCollegeId(@Param("collegeId") Long collegeId);
}


package org.collegemanagement.repositories;

import org.collegemanagement.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LibrarianRepository extends JpaRepository<User, Long> {

    /**
     * Find all librarians by college ID with role filtering
     */
    @Query("""
            SELECT u FROM User u
            JOIN u.roles r
            WHERE u.college.id = :collegeId
            AND r.name = 'ROLE_LIBRARIAN'
            ORDER BY u.name ASC
            """)
    Page<User> findAllLibrariansByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find librarian by ID and college ID with role verification
     */
    @Query("""
            SELECT u FROM User u
            JOIN u.roles r
            WHERE u.id = :librarianId
            AND u.college.id = :collegeId
            AND r.name = 'ROLE_LIBRARIAN'
            """)
    Optional<User> findLibrarianByIdAndCollegeId(@Param("librarianId") Long librarianId, @Param("collegeId") Long collegeId);

    /**
     * Find librarian by UUID and college ID with role verification
     */
    @Query("""
            SELECT u FROM User u
            JOIN u.roles r
            WHERE u.uuid = :uuid
            AND u.college.id = :collegeId
            AND r.name = 'ROLE_LIBRARIAN'
            """)
    Optional<User> findLibrarianByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Check if librarian exists by email and college ID
     */
    @Query("""
            SELECT COUNT(u) > 0 FROM User u
            JOIN u.roles r
            WHERE u.email = :email
            AND u.college.id = :collegeId
            AND r.name = 'ROLE_LIBRARIAN'
            """)
    boolean existsByEmailAndCollegeId(@Param("email") String email, @Param("collegeId") Long collegeId);

    /**
     * Check if librarian exists by email, college ID, and not the given ID (for updates)
     */
    @Query("""
            SELECT COUNT(u) > 0 FROM User u
            JOIN u.roles r
            WHERE u.email = :email
            AND u.college.id = :collegeId
            AND u.id != :excludeId
            AND r.name = 'ROLE_LIBRARIAN'
            """)
    boolean existsByEmailAndCollegeIdAndIdNot(@Param("email") String email, @Param("collegeId") Long collegeId, @Param("excludeId") Long excludeId);

    /**
     * Search librarians by name or email within a college
     */
    @Query("""
            SELECT u FROM User u
            JOIN u.roles r
            WHERE u.college.id = :collegeId
            AND r.name = 'ROLE_LIBRARIAN'
            AND (LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
            ORDER BY u.name ASC
            """)
    Page<User> searchLibrariansByCollegeId(@Param("collegeId") Long collegeId, @Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Count librarians by college ID
     */
    @Query("""
            SELECT COUNT(u) FROM User u
            JOIN u.roles r
            WHERE u.college.id = :collegeId
            AND r.name = 'ROLE_LIBRARIAN'
            """)
    long countByCollegeId(@Param("collegeId") Long collegeId);
}


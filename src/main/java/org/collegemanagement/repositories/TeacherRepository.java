package org.collegemanagement.repositories;

import org.collegemanagement.entity.user.Role;
import org.collegemanagement.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<User, Long> {

    /**
     * Find all teachers by college ID with role filtering
     */
    @Query("""
            SELECT u FROM User u
            JOIN u.roles r
            WHERE u.college.id = :collegeId
            AND r.name = 'ROLE_TEACHER'
            ORDER BY u.name ASC
            """)
    Page<User> findAllTeachersByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find teacher by ID and college ID with role verification
     */
    @Query("""
            SELECT u FROM User u
            JOIN u.roles r
            WHERE u.id = :teacherId
            AND u.college.id = :collegeId
            AND r.name = 'ROLE_TEACHER'
            """)
    Optional<User> findTeacherByIdAndCollegeId(@Param("teacherId") Long teacherId, @Param("collegeId") Long collegeId);

    /**
     * Find teacher by UUID and college ID with role verification
     */
    @Query("""
            SELECT u FROM User u
            JOIN u.roles r
            WHERE u.uuid = :uuid
            AND u.college.id = :collegeId
            AND r.name = 'ROLE_TEACHER'
            """)
    Optional<User> findTeacherByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Check if teacher exists by email and college ID
     */
    @Query("""
            SELECT COUNT(u) > 0 FROM User u
            JOIN u.roles r
            WHERE u.email = :email
            AND u.college.id = :collegeId
            AND r.name = 'ROLE_TEACHER'
            """)
    boolean existsByEmailAndCollegeId(@Param("email") String email, @Param("collegeId") Long collegeId);

    /**
     * Check if teacher exists by email, college ID, and not the given ID (for updates)
     */
    @Query("""
            SELECT COUNT(u) > 0 FROM User u
            JOIN u.roles r
            WHERE u.email = :email
            AND u.college.id = :collegeId
            AND u.id != :excludeId
            AND r.name = 'ROLE_TEACHER'
            """)
    boolean existsByEmailAndCollegeIdAndIdNot(@Param("email") String email, @Param("collegeId") Long collegeId, @Param("excludeId") Long excludeId);

    /**
     * Search teachers by name or email within a college
     */
    @Query("""
            SELECT u FROM User u
            JOIN u.roles r
            WHERE u.college.id = :collegeId
            AND r.name = 'ROLE_TEACHER'
            AND (LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
            ORDER BY u.name ASC
            """)
    Page<User> searchTeachersByCollegeId(@Param("collegeId") Long collegeId, @Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Count teachers by college ID
     */
    @Query("""
            SELECT COUNT(u) FROM User u
            JOIN u.roles r
            WHERE u.college.id = :collegeId
            AND r.name = 'ROLE_TEACHER'
            """)
    long countByCollegeId(@Param("collegeId") Long collegeId);

    /**
     * Find teachers by department (if they are head of department)
     */
    @Query("""
            SELECT u FROM User u
            JOIN u.roles r
            JOIN u.headedDepartments d
            WHERE d.id = :departmentId
            AND u.college.id = :collegeId
            AND r.name = 'ROLE_TEACHER'
            """)
    List<User> findTeachersByDepartmentId(@Param("departmentId") Long departmentId, @Param("collegeId") Long collegeId);
}


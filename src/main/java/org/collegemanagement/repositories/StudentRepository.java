package org.collegemanagement.repositories;

import org.collegemanagement.entity.student.Student;
import org.collegemanagement.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * Find student by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT s FROM Student s
            WHERE s.uuid = :uuid
            AND s.college.id = :collegeId
            """)
    Optional<Student> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find student by user ID and college ID
     */
    @Query("""
            SELECT s FROM Student s
            WHERE s.user.id = :userId
            AND s.college.id = :collegeId
            """)
    Optional<Student> findByUserIdAndCollegeId(@Param("userId") Long userId, @Param("collegeId") Long collegeId);

    /**
     * Find all students by college ID with pagination
     */
    @Query("""
            SELECT s FROM Student s
            WHERE s.college.id = :collegeId
            ORDER BY s.admissionDate DESC, s.rollNumber ASC
            """)
    Page<Student> findAllByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find students by status and college ID
     */
    @Query("""
            SELECT s FROM Student s
            WHERE s.status = :status
            AND s.college.id = :collegeId
            ORDER BY s.rollNumber ASC
            """)
    Page<Student> findByStatusAndCollegeId(@Param("status") Status status, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Search students by name, roll number, or registration number within a college
     */
    @Query("""
            SELECT s FROM Student s
            WHERE s.college.id = :collegeId
            AND (LOWER(s.user.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(s.rollNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(s.registrationNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(s.user.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
            ORDER BY s.rollNumber ASC
            """)
    Page<Student> searchStudentsByCollegeId(@Param("collegeId") Long collegeId, @Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Check if roll number exists within a college
     */
    @Query("""
            SELECT COUNT(s) > 0 FROM Student s
            WHERE s.rollNumber = :rollNumber
            AND s.college.id = :collegeId
            """)
    boolean existsByRollNumberAndCollegeId(@Param("rollNumber") String rollNumber, @Param("collegeId") Long collegeId);

    /**
     * Check if registration number exists within a college
     */
    @Query("""
            SELECT COUNT(s) > 0 FROM Student s
            WHERE s.registrationNumber = :registrationNumber
            AND s.college.id = :collegeId
            """)
    boolean existsByRegistrationNumberAndCollegeId(@Param("registrationNumber") String registrationNumber, @Param("collegeId") Long collegeId);

    /**
     * Check if roll number exists within a college (excluding a specific student for updates)
     */
    @Query("""
            SELECT COUNT(s) > 0 FROM Student s
            WHERE s.rollNumber = :rollNumber
            AND s.college.id = :collegeId
            AND s.id != :excludeId
            """)
    boolean existsByRollNumberAndCollegeIdAndIdNot(@Param("rollNumber") String rollNumber, @Param("collegeId") Long collegeId, @Param("excludeId") Long excludeId);

    /**
     * Check if registration number exists within a college (excluding a specific student for updates)
     */
    @Query("""
            SELECT COUNT(s) > 0 FROM Student s
            WHERE s.registrationNumber = :registrationNumber
            AND s.college.id = :collegeId
            AND s.id != :excludeId
            """)
    boolean existsByRegistrationNumberAndCollegeIdAndIdNot(@Param("registrationNumber") String registrationNumber, @Param("collegeId") Long collegeId, @Param("excludeId") Long excludeId);


    /**
     * Count students by status and college ID
     */
    @Query("""
            SELECT COUNT(s) FROM Student s
            WHERE s.status = :status
            AND s.college.id = :collegeId
            """)
    long countByStatusAndCollegeId(@Param("status") Status status, @Param("collegeId") Long collegeId);

    /**
     * Count total students by college ID
     */
    @Query("""
            SELECT COUNT(s) FROM Student s
            WHERE s.college.id = :collegeId
            """)
    long countByCollegeId(@Param("collegeId") Long collegeId);
}


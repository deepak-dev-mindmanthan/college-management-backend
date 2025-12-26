package org.collegemanagement.repositories;

import org.collegemanagement.entity.academic.StudentEnrollment;
import org.collegemanagement.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentEnrollmentRepository extends JpaRepository<StudentEnrollment, Long> {

    /**
     * Find enrollment by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT e FROM StudentEnrollment e
            WHERE e.uuid = :uuid
            AND e.college.id = :collegeId
            """)
    Optional<StudentEnrollment> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find enrollment by student ID and academic year ID
     */
    @Query("""
            SELECT e FROM StudentEnrollment e
            WHERE e.student.id = :studentId
            AND e.academicYear.id = :academicYearId
            """)
    Optional<StudentEnrollment> findByStudentIdAndAcademicYearId(@Param("studentId") Long studentId, @Param("academicYearId") Long academicYearId);

    /**
     * Find all enrollments by student ID and college ID
     */
    @Query("""
            SELECT e FROM StudentEnrollment e
            WHERE e.student.id = :studentId
            AND e.college.id = :collegeId
            ORDER BY e.academicYear.startDate DESC
            """)
    List<StudentEnrollment> findByStudentIdAndCollegeId(@Param("studentId") Long studentId, @Param("collegeId") Long collegeId);

    /**
     * Find active enrollment by student ID and college ID
     */
    @Query("""
            SELECT e FROM StudentEnrollment e
            WHERE e.student.id = :studentId
            AND e.college.id = :collegeId
            AND e.status = 'ACTIVE'
            ORDER BY e.academicYear.startDate DESC
            """)
    Optional<StudentEnrollment> findActiveByStudentIdAndCollegeId(@Param("studentId") Long studentId, @Param("collegeId") Long collegeId);

    /**
     * Find enrollments by class ID and college ID
     */
    @Query("""
            SELECT e FROM StudentEnrollment e
            WHERE e.classRoom.id = :classId
            AND e.college.id = :collegeId
            AND e.status = 'ACTIVE'
            ORDER BY e.rollNumber ASC
            """)
    List<StudentEnrollment> findByClassIdAndCollegeId(@Param("classId") Long classId, @Param("collegeId") Long collegeId);

    /**
     * Find enrollments by class UUID and college ID
     */
    @Query("""
            SELECT e FROM StudentEnrollment e
            WHERE e.classRoom.uuid = :classUuid
            AND e.college.id = :collegeId
            AND e.status = 'ACTIVE'
            ORDER BY e.rollNumber ASC
            """)
    List<StudentEnrollment> findByClassUuidAndCollegeId(@Param("classUuid") String classUuid, @Param("collegeId") Long collegeId);

    /**
     * Find enrollments by academic year ID and college ID
     */
    @Query("""
            SELECT e FROM StudentEnrollment e
            WHERE e.academicYear.id = :academicYearId
            AND e.college.id = :collegeId
            ORDER BY e.rollNumber ASC
            """)
    List<StudentEnrollment> findByAcademicYearIdAndCollegeId(@Param("academicYearId") Long academicYearId, @Param("collegeId") Long collegeId);

    /**
     * Find enrollments by academic year UUID and college ID
     */
    @Query("""
            SELECT e FROM StudentEnrollment e
            WHERE e.academicYear.uuid = :academicYearUuid
            AND e.college.id = :collegeId
            ORDER BY e.rollNumber ASC
            """)
    List<StudentEnrollment> findByAcademicYearUuidAndCollegeId(@Param("academicYearUuid") String academicYearUuid, @Param("collegeId") Long collegeId);

    /**
     * Find enrollments by status and college ID
     */
    @Query("""
            SELECT e FROM StudentEnrollment e
            WHERE e.status = :status
            AND e.college.id = :collegeId
            ORDER BY e.academicYear.startDate DESC
            """)
    List<StudentEnrollment> findByStatusAndCollegeId(@Param("status") EnrollmentStatus status, @Param("collegeId") Long collegeId);

    /**
     * Check if enrollment exists for student and academic year
     */
    @Query("""
            SELECT COUNT(e) > 0 FROM StudentEnrollment e
            WHERE e.student.id = :studentId
            AND e.academicYear.id = :academicYearId
            """)
    boolean existsByStudentIdAndAcademicYearId(@Param("studentId") Long studentId, @Param("academicYearId") Long academicYearId);
}


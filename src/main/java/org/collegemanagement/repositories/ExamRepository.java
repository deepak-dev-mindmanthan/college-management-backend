package org.collegemanagement.repositories;

import org.collegemanagement.entity.exam.Exam;
import org.collegemanagement.enums.ExamType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    /**
     * Find exam by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT e FROM Exam e
            WHERE e.uuid = :uuid
            AND e.college.id = :collegeId
            """)
    Optional<Exam> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find all exams by college ID with pagination
     */
    @Query("""
            SELECT e FROM Exam e
            WHERE e.college.id = :collegeId
            ORDER BY e.startDate DESC, e.createdAt DESC
            """)
    Page<Exam> findAllByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find exams by type and college ID
     */
    @Query("""
            SELECT e FROM Exam e
            WHERE e.examType = :examType
            AND e.college.id = :collegeId
            ORDER BY e.startDate DESC
            """)
    Page<Exam> findByExamTypeAndCollegeId(@Param("examType") ExamType examType, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find exams by academic year and college ID
     */
    @Query("""
            SELECT e FROM Exam e
            WHERE e.academicYear.uuid = :academicYearUuid
            AND e.college.id = :collegeId
            ORDER BY e.startDate DESC
            """)
    Page<Exam> findByAcademicYearUuidAndCollegeId(@Param("academicYearUuid") String academicYearUuid, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Check if exam name exists for a college and academic year
     */
    @Query("""
            SELECT COUNT(e) > 0 FROM Exam e
            WHERE e.name = :name
            AND e.college.id = :collegeId
            AND e.academicYear.id = :academicYearId
            """)
    boolean existsByNameAndCollegeIdAndAcademicYearId(@Param("name") String name, @Param("collegeId") Long collegeId, @Param("academicYearId") Long academicYearId);

    /**
     * Check if exam name exists for a college and academic year excluding a specific exam (for updates)
     */
    @Query("""
            SELECT COUNT(e) > 0 FROM Exam e
            WHERE e.name = :name
            AND e.college.id = :collegeId
            AND e.academicYear.id = :academicYearId
            AND e.id != :excludeId
            """)
    boolean existsByNameAndCollegeIdAndAcademicYearIdAndIdNot(@Param("name") String name, @Param("collegeId") Long collegeId, @Param("academicYearId") Long academicYearId, @Param("excludeId") Long excludeId);

    /**
     * Search exams by name within a college
     */
    @Query("""
            SELECT e FROM Exam e
            WHERE e.college.id = :collegeId
            AND LOWER(e.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            ORDER BY e.startDate DESC
            """)
    Page<Exam> searchByCollegeId(@Param("collegeId") Long collegeId, @Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find exams by date range and college ID
     */
    @Query("""
            SELECT e FROM Exam e
            WHERE e.college.id = :collegeId
            AND e.startDate >= :startDate
            AND e.endDate <= :endDate
            ORDER BY e.startDate ASC
            """)
    Page<Exam> findByDateRangeAndCollegeId(@Param("collegeId") Long collegeId, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate, Pageable pageable);

    /**
     * Count exams by college ID
     */
    @Query("""
            SELECT COUNT(e) FROM Exam e
            WHERE e.college.id = :collegeId
            """)
    long countByCollegeId(@Param("collegeId") Long collegeId);

    /**
     * Find all exams by college ID (without pagination)
     */
    @Query("""
            SELECT e FROM Exam e
            WHERE e.college.id = :collegeId
            ORDER BY e.startDate DESC, e.createdAt DESC
            """)
    List<Exam> findAllByCollegeId(@Param("collegeId") Long collegeId);
}


package org.collegemanagement.repositories;

import org.collegemanagement.entity.exam.GradeScale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface GradeScaleRepository extends JpaRepository<GradeScale, Long> {

    /**
     * Find grade scale by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT gs FROM GradeScale gs
            WHERE gs.uuid = :uuid
            AND gs.college.id = :collegeId
            """)
    Optional<GradeScale> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find all grade scales by college ID with pagination
     */
    @Query("""
            SELECT gs FROM GradeScale gs
            WHERE gs.college.id = :collegeId
            ORDER BY gs.minMarks DESC
            """)
    Page<GradeScale> findAllByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find all grade scales by college ID (without pagination)
     */
    @Query("""
            SELECT gs FROM GradeScale gs
            WHERE gs.college.id = :collegeId
            ORDER BY gs.minMarks DESC
            """)
    List<GradeScale> findAllByCollegeId(@Param("collegeId") Long collegeId);

    /**
     * Check if grade exists for a college
     */
    @Query("""
            SELECT COUNT(gs) > 0 FROM GradeScale gs
            WHERE gs.grade = :grade
            AND gs.college.id = :collegeId
            """)
    boolean existsByGradeAndCollegeId(@Param("grade") String grade, @Param("collegeId") Long collegeId);

    /**
     * Check if grade exists for a college excluding a specific grade scale (for updates)
     */
    @Query("""
            SELECT COUNT(gs) > 0 FROM GradeScale gs
            WHERE gs.grade = :grade
            AND gs.college.id = :collegeId
            AND gs.id != :excludeId
            """)
    boolean existsByGradeAndCollegeIdAndIdNot(@Param("grade") String grade, @Param("collegeId") Long collegeId, @Param("excludeId") Long excludeId);

    /**
     * Find grade scale by marks range and college ID
     */
    @Query("""
            SELECT gs FROM GradeScale gs
            WHERE gs.college.id = :collegeId
            AND :marks >= gs.minMarks
            AND :marks <= gs.maxMarks
            ORDER BY gs.minMarks DESC
            """)
    Optional<GradeScale> findGradeByMarksAndCollegeId(@Param("marks") Integer marks, @Param("collegeId") Long collegeId);

    /**
     * Find grade scale by marks percentage and college ID
     * Assuming maxMarks is 100 for percentage calculation
     */
    @Query("""
            SELECT gs FROM GradeScale gs
            WHERE gs.college.id = :collegeId
            AND :percentage >= gs.minMarks
            AND :percentage <= gs.maxMarks
            ORDER BY gs.minMarks DESC
            """)
    Optional<GradeScale> findGradeByPercentageAndCollegeId(@Param("percentage") BigDecimal percentage, @Param("collegeId") Long collegeId);
}


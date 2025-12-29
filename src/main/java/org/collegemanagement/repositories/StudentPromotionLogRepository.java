package org.collegemanagement.repositories;

import org.collegemanagement.entity.academic.StudentPromotionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentPromotionLogRepository extends JpaRepository<StudentPromotionLog, Long> {

    /**
     * Find promotion log by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT pl FROM StudentPromotionLog pl
            WHERE pl.uuid = :uuid
            AND pl.college.id = :collegeId
            """)
    Optional<StudentPromotionLog> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find all promotion logs by college ID with pagination
     */
    @Query("""
            SELECT pl FROM StudentPromotionLog pl
            WHERE pl.college.id = :collegeId
            ORDER BY pl.createdAt DESC
            """)
    Page<StudentPromotionLog> findAllByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find promotion logs by student UUID and college ID
     */
    @Query("""
            SELECT pl FROM StudentPromotionLog pl
            WHERE pl.student.uuid = :studentUuid
            AND pl.college.id = :collegeId
            ORDER BY pl.createdAt DESC
            """)
    Page<StudentPromotionLog> findByStudentUuidAndCollegeId(
            @Param("studentUuid") String studentUuid,
            @Param("collegeId") Long collegeId,
            Pageable pageable
    );

    /**
     * Find promotion logs by academic year UUID and college ID
     */
    @Query("""
            SELECT pl FROM StudentPromotionLog pl
            WHERE pl.academicYear.uuid = :academicYearUuid
            AND pl.college.id = :collegeId
            ORDER BY pl.createdAt DESC
            """)
    Page<StudentPromotionLog> findByAcademicYearUuidAndCollegeId(
            @Param("academicYearUuid") String academicYearUuid,
            @Param("collegeId") Long collegeId,
            Pageable pageable
    );

    /**
     * Find all promotion logs for a student (for promotion history)
     */
    @Query("""
            SELECT pl FROM StudentPromotionLog pl
            WHERE pl.student.uuid = :studentUuid
            AND pl.college.id = :collegeId
            ORDER BY pl.createdAt DESC
            """)
    List<StudentPromotionLog> findAllByStudentUuidAndCollegeId(
            @Param("studentUuid") String studentUuid,
            @Param("collegeId") Long collegeId
    );
}


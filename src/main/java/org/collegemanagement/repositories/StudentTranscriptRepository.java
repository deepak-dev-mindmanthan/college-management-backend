package org.collegemanagement.repositories;

import org.collegemanagement.entity.exam.StudentTranscript;
import org.collegemanagement.enums.ResultStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentTranscriptRepository extends JpaRepository<StudentTranscript, Long> {

    /**
     * Find student transcript by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT st FROM StudentTranscript st
            JOIN st.student s
            WHERE st.uuid = :uuid
            AND s.college.id = :collegeId
            """)
    Optional<StudentTranscript> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find student transcript by student UUID and academic year UUID and college ID
     */
    @Query("""
            SELECT st FROM StudentTranscript st
            JOIN st.student s
            WHERE st.student.uuid = :studentUuid
            AND st.academicYear.uuid = :academicYearUuid
            AND s.college.id = :collegeId
            """)
    Optional<StudentTranscript> findByStudentUuidAndAcademicYearUuidAndCollegeId(
            @Param("studentUuid") String studentUuid,
            @Param("academicYearUuid") String academicYearUuid,
            @Param("collegeId") Long collegeId);

    /**
     * Find all student transcripts by student UUID and college ID
     */
    @Query("""
            SELECT st FROM StudentTranscript st
            JOIN st.student s
            WHERE st.student.uuid = :studentUuid
            AND s.college.id = :collegeId
            ORDER BY st.academicYear.startDate DESC
            """)
    List<StudentTranscript> findByStudentUuidAndCollegeId(@Param("studentUuid") String studentUuid, @Param("collegeId") Long collegeId);

    /**
     * Find all student transcripts by academic year UUID and college ID with pagination
     */
    @Query("""
            SELECT st FROM StudentTranscript st
            JOIN st.student s
            WHERE st.academicYear.uuid = :academicYearUuid
            AND s.college.id = :collegeId
            ORDER BY st.student.rollNumber ASC
            """)
    Page<StudentTranscript> findByAcademicYearUuidAndCollegeId(@Param("academicYearUuid") String academicYearUuid, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find published student transcripts by academic year UUID and college ID
     */
    @Query("""
            SELECT st FROM StudentTranscript st
            JOIN st.student s
            WHERE st.academicYear.uuid = :academicYearUuid
            AND s.college.id = :collegeId
            AND st.published = true
            ORDER BY st.student.rollNumber ASC
            """)
    Page<StudentTranscript> findPublishedByAcademicYearUuidAndCollegeId(@Param("academicYearUuid") String academicYearUuid, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find student transcripts by result status and college ID
     */
    @Query("""
            SELECT st FROM StudentTranscript st
            JOIN st.student s
            WHERE st.resultStatus = :resultStatus
            AND s.college.id = :collegeId
            ORDER BY st.academicYear.startDate DESC
            """)
    Page<StudentTranscript> findByResultStatusAndCollegeId(@Param("resultStatus") ResultStatus resultStatus, @Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Check if student transcript already exists (student and academic year combination)
     */
    @Query("""
            SELECT COUNT(st) > 0 FROM StudentTranscript st
            WHERE st.student.id = :studentId
            AND st.academicYear.id = :academicYearId
            """)
    boolean existsByStudentIdAndAcademicYearId(@Param("studentId") Long studentId, @Param("academicYearId") Long academicYearId);

    /**
     * Count published transcripts by academic year and college ID
     */
    @Query("""
            SELECT COUNT(st) FROM StudentTranscript st
            JOIN st.student s
            WHERE st.academicYear.uuid = :academicYearUuid
            AND s.college.id = :collegeId
            AND st.published = true
            """)
    long countPublishedByAcademicYearUuidAndCollegeId(@Param("academicYearUuid") String academicYearUuid, @Param("collegeId") Long collegeId);
}


package org.collegemanagement.repositories;

import org.collegemanagement.entity.exam.ExamSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExamSubjectRepository extends JpaRepository<ExamSubject, Long> {

    /**
     * Find exam subject by UUID
     */
    Optional<ExamSubject> findByUuid(String uuid);

    /**
     * Find exam subject by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT es FROM ExamSubject es
            JOIN es.examClass ec
            JOIN ec.exam e
            WHERE es.uuid = :uuid
            AND e.college.id = :collegeId
            """)
    Optional<ExamSubject> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find exam subjects by exam class UUID and college ID
     */
    @Query("""
            SELECT es FROM ExamSubject es
            JOIN es.examClass ec
            JOIN ec.exam e
            WHERE ec.uuid = :examClassUuid
            AND e.college.id = :collegeId
            ORDER BY es.examDate ASC
            """)
    List<ExamSubject> findByExamClassUuidAndCollegeId(@Param("examClassUuid") String examClassUuid, @Param("collegeId") Long collegeId);

    /**
     * Check if exam subject already exists (exam class and subject combination)
     */
    @Query("""
            SELECT COUNT(es) > 0 FROM ExamSubject es
            WHERE es.examClass.id = :examClassId
            AND es.subject.id = :subjectId
            """)
    boolean existsByExamClassIdAndSubjectId(@Param("examClassId") Long examClassId, @Param("subjectId") Long subjectId);

    /**
     * Find exam subjects by exam UUID and college ID
     */
    @Query("""
            SELECT es FROM ExamSubject es
            JOIN es.examClass ec
            JOIN ec.exam e
            WHERE e.uuid = :examUuid
            AND e.college.id = :collegeId
            ORDER BY es.examDate ASC
            """)
    List<ExamSubject> findByExamUuidAndCollegeId(@Param("examUuid") String examUuid, @Param("collegeId") Long collegeId);

    /**
     * Find exam subjects by date range and college ID
     */
    @Query("""
            SELECT es FROM ExamSubject es
            JOIN es.examClass ec
            JOIN ec.exam e
            WHERE e.college.id = :collegeId
            AND es.examDate >= :startDate
            AND es.examDate <= :endDate
            ORDER BY es.examDate ASC
            """)
    List<ExamSubject> findByDateRangeAndCollegeId(@Param("collegeId") Long collegeId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}


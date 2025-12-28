package org.collegemanagement.repositories;

import org.collegemanagement.entity.exam.StudentMarks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentMarksRepository extends JpaRepository<StudentMarks, Long> {

    /**
     * Find student marks by UUID
     */
    Optional<StudentMarks> findByUuid(String uuid);

    /**
     * Find student marks by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT sm FROM StudentMarks sm
            JOIN sm.examSubject es
            JOIN es.examClass ec
            JOIN ec.exam e
            WHERE sm.uuid = :uuid
            AND e.college.id = :collegeId
            """)
    Optional<StudentMarks> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find student marks by exam subject UUID and college ID
     */
    @Query("""
            SELECT sm FROM StudentMarks sm
            JOIN sm.examSubject es
            JOIN es.examClass ec
            JOIN ec.exam e
            WHERE es.uuid = :examSubjectUuid
            AND e.college.id = :collegeId
            ORDER BY sm.student.rollNumber ASC
            """)
    List<StudentMarks> findByExamSubjectUuidAndCollegeId(@Param("examSubjectUuid") String examSubjectUuid, @Param("collegeId") Long collegeId);

    /**
     * Find student marks by student UUID and exam UUID and college ID
     */
    @Query("""
            SELECT sm FROM StudentMarks sm
            JOIN sm.examSubject es
            JOIN es.examClass ec
            JOIN ec.exam e
            WHERE sm.student.uuid = :studentUuid
            AND e.uuid = :examUuid
            AND e.college.id = :collegeId
            ORDER BY es.examDate ASC
            """)
    List<StudentMarks> findByStudentUuidAndExamUuidAndCollegeId(@Param("studentUuid") String studentUuid, @Param("examUuid") String examUuid, @Param("collegeId") Long collegeId);

    /**
     * Find student marks by student UUID and college ID
     */
    @Query("""
            SELECT sm FROM StudentMarks sm
            JOIN sm.examSubject es
            JOIN es.examClass ec
            JOIN ec.exam e
            WHERE sm.student.uuid = :studentUuid
            AND e.college.id = :collegeId
            ORDER BY e.startDate DESC, es.examDate ASC
            """)
    List<StudentMarks> findByStudentUuidAndCollegeId(@Param("studentUuid") String studentUuid, @Param("collegeId") Long collegeId);

    /**
     * Check if student marks already exists (exam subject and student combination)
     */
    @Query("""
            SELECT COUNT(sm) > 0 FROM StudentMarks sm
            WHERE sm.examSubject.id = :examSubjectId
            AND sm.student.id = :studentId
            """)
    boolean existsByExamSubjectIdAndStudentId(@Param("examSubjectId") Long examSubjectId, @Param("studentId") Long studentId);

    /**
     * Find student marks by exam subject and student
     */
    @Query("""
            SELECT sm FROM StudentMarks sm
            WHERE sm.examSubject.id = :examSubjectId
            AND sm.student.id = :studentId
            """)
    Optional<StudentMarks> findByExamSubjectIdAndStudentId(@Param("examSubjectId") Long examSubjectId, @Param("studentId") Long studentId);

    /**
     * Count students with marks for an exam subject
     */
    @Query("""
            SELECT COUNT(sm) FROM StudentMarks sm
            WHERE sm.examSubject.id = :examSubjectId
            """)
    long countByExamSubjectId(@Param("examSubjectId") Long examSubjectId);

    /**
     * Find student marks by class UUID and exam UUID and college ID
     */
    @Query("""
            SELECT sm FROM StudentMarks sm
            JOIN sm.examSubject es
            JOIN es.examClass ec
            JOIN ec.exam e
            WHERE ec.classRoom.uuid = :classUuid
            AND e.uuid = :examUuid
            AND e.college.id = :collegeId
            ORDER BY sm.student.rollNumber ASC
            """)
    List<StudentMarks> findByClassUuidAndExamUuidAndCollegeId(@Param("classUuid") String classUuid, @Param("examUuid") String examUuid, @Param("collegeId") Long collegeId);
}


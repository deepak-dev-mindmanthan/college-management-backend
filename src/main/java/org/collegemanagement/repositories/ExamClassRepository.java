package org.collegemanagement.repositories;

import org.collegemanagement.entity.exam.ExamClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExamClassRepository extends JpaRepository<ExamClass, Long> {

    /**
     * Find exam class by UUID
     */
    Optional<ExamClass> findByUuid(String uuid);

    /**
     * Find exam class by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT ec FROM ExamClass ec
            JOIN ec.exam e
            WHERE ec.uuid = :uuid
            AND e.college.id = :collegeId
            """)
    Optional<ExamClass> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find exam classes by exam UUID and college ID
     */
    @Query("""
            SELECT ec FROM ExamClass ec
            JOIN ec.exam e
            WHERE e.uuid = :examUuid
            AND e.college.id = :collegeId
            ORDER BY ec.classRoom.name ASC, ec.classRoom.section ASC
            """)
    List<ExamClass> findByExamUuidAndCollegeId(@Param("examUuid") String examUuid, @Param("collegeId") Long collegeId);

    /**
     * Check if exam class already exists (exam and class combination)
     */
    @Query("""
            SELECT COUNT(ec) > 0 FROM ExamClass ec
            WHERE ec.exam.id = :examId
            AND ec.classRoom.id = :classId
            """)
    boolean existsByExamIdAndClassRoomId(@Param("examId") Long examId, @Param("classId") Long classId);

    /**
     * Find exam class by exam and class
     */
    @Query("""
            SELECT ec FROM ExamClass ec
            WHERE ec.exam.id = :examId
            AND ec.classRoom.id = :classId
            """)
    Optional<ExamClass> findByExamIdAndClassRoomId(@Param("examId") Long examId, @Param("classId") Long classId);
}


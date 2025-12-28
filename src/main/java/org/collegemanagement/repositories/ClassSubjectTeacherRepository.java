package org.collegemanagement.repositories;

import org.collegemanagement.entity.academic.ClassSubjectTeacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClassSubjectTeacherRepository extends JpaRepository<ClassSubjectTeacher, Long> {

    @Query("""
            SELECT cst FROM ClassSubjectTeacher cst
            WHERE cst.teacher.id = :teacherId
            AND cst.classRoom.college.id = :collegeId
            """)
    List<ClassSubjectTeacher> findByTeacherIdAndCollegeId(@Param("teacherId") Long teacherId, @Param("collegeId") Long collegeId);

    @Query("""
            SELECT cst FROM ClassSubjectTeacher cst
            WHERE cst.classRoom.uuid = :classUuid
            AND cst.subject.uuid = :subjectUuid
            AND cst.teacher.id = :teacherId
            AND cst.classRoom.college.id = :collegeId
            """)
    Optional<ClassSubjectTeacher> findByClassUuidAndSubjectUuidAndTeacherIdAndCollegeId(
            @Param("classUuid") String classUuid,
            @Param("subjectUuid") String subjectUuid,
            @Param("teacherId") Long teacherId,
            @Param("collegeId") Long collegeId
    );

    @Query("""
            SELECT COUNT(cst) > 0 FROM ClassSubjectTeacher cst
            WHERE cst.classRoom.uuid = :classUuid
            AND cst.subject.uuid = :subjectUuid
            AND cst.teacher.id = :teacherId
            AND cst.classRoom.college.id = :collegeId
            """)
    boolean existsByClassUuidAndSubjectUuidAndTeacherIdAndCollegeId(
            @Param("classUuid") String classUuid,
            @Param("subjectUuid") String subjectUuid,
            @Param("teacherId") Long teacherId,
            @Param("collegeId") Long collegeId
    );

    /**
     * Find ClassSubjectTeacher by class UUID and subject UUID (for auto-assignment)
     */
    @Query("""
            SELECT cst FROM ClassSubjectTeacher cst
            WHERE cst.classRoom.uuid = :classUuid
            AND cst.subject.uuid = :subjectUuid
            AND cst.classRoom.college.id = :collegeId
            """)
    Optional<ClassSubjectTeacher> findByClassUuidAndSubjectUuidAndCollegeId(
            @Param("classUuid") String classUuid,
            @Param("subjectUuid") String subjectUuid,
            @Param("collegeId") Long collegeId
    );
}


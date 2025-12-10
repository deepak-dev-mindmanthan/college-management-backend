package org.collegemanagement.repositories;

import org.collegemanagement.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByCourseId(Long courseId);

    @Query("SELECT COUNT(s) FROM Subject s WHERE s.teacher.id = :teacherId")
    long countByTeacherId(@Param("teacherId") Long teacherId);

    @Query("SELECT COUNT(s) FROM Subject s WHERE s.course.college.id = :collegeId")
    long countSubjectsByCollegeId(@Param("collegeId") Long collegeId);

}
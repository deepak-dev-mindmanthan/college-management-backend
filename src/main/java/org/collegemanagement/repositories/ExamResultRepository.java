package org.collegemanagement.repositories;

import org.collegemanagement.entity.ExamResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {
    List<ExamResult> findByStudentId(long id);
    @Query("SELECT r FROM ExamResult r WHERE r.student.id = :studentId")
    List<ExamResult> getStudentResults(@Param("studentId") Long studentId);

}

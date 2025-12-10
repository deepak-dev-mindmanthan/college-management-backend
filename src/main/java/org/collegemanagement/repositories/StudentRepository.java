package org.collegemanagement.repositories;

import org.collegemanagement.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<Student, Long> {
    @Query("SELECT COUNT(s) FROM Subject s WHERE s.teacher.id = :teacherId")
    long countByTeacherId(@Param("teacherId") Long teacherId);

}

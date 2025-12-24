package org.collegemanagement.repositories;

import org.collegemanagement.entity.exam.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamRepository extends JpaRepository<Exam, Long> {
}

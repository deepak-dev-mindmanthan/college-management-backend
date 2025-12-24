package org.collegemanagement.services;

import org.collegemanagement.entity.exam.Exam;

public interface ExamService {
    Exam createExam(Exam exam);
    Exam findExamById(Long id);
}

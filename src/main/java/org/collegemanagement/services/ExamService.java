package org.collegemanagement.services;

import org.collegemanagement.entity.Exam;

import java.util.List;

public interface ExamService {
    Exam createExam(Exam exam);
    Exam findExamById(Long id);
}

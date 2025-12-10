package org.collegemanagement.services;

import org.collegemanagement.entity.ExamResult;

import java.util.List;

public interface ExamResultService {
    List<ExamResult> findByStudentId(long id);
    ExamResult createExamResult(ExamResult examResult);
    List<ExamResult> getStudentResults(long studentId);
}

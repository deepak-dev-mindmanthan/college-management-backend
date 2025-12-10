package org.collegemanagement.services.impl;

import org.collegemanagement.entity.ExamResult;
import org.collegemanagement.repositories.ExamResultRepository;
import org.collegemanagement.services.ExamResultService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class ExamResultServiceImpl implements ExamResultService {
    private final ExamResultRepository examResultRepository;

    public ExamResultServiceImpl(ExamResultRepository examResultRepository) {
        this.examResultRepository = examResultRepository;
    }

    @Override
    public List<ExamResult> findByStudentId(long id) {
        return examResultRepository.findByStudentId(id);
    }

    @Transactional
    @Override
    public ExamResult createExamResult(ExamResult examResult) {
        return examResultRepository.save(examResult);
    }

    @Override
    public List<ExamResult> getStudentResults(long studentId) {
        return examResultRepository.getStudentResults(studentId);
    }
}

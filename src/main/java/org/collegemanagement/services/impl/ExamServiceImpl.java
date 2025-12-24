package org.collegemanagement.services.impl;

import org.collegemanagement.entity.exam.Exam;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.repositories.ExamRepository;
import org.collegemanagement.services.ExamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;

    public ExamServiceImpl(ExamRepository examRepository) {
        this.examRepository = examRepository;
    }

    @Transactional
    @Override
    public Exam createExam(Exam exam) {
        return examRepository.save(exam);
    }

    @Override
    public Exam findExamById(Long id) {
        return examRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Exam not found with id:"+id));
    }
}

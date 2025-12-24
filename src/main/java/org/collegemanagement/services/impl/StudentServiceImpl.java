package org.collegemanagement.services.impl;

import org.collegemanagement.repositories.StudentRepository;
import org.collegemanagement.services.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class StudentServiceImpl implements StudentService {
    private final StudentRepository studentRepository;

    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public long countByTeacherId(Long teacherId) {
        return 0;
    }
}

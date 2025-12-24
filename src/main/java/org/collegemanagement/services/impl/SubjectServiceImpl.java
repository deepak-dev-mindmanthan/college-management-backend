package org.collegemanagement.services.impl;


import org.collegemanagement.entity.academic.Subject;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.repositories.SubjectRepository;
import org.collegemanagement.services.SubjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubjectServiceImpl  implements SubjectService {
    private final SubjectRepository subjectRepository;

    public SubjectServiceImpl(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @Override
    public List<Subject> findByCourseId(long id) {
        return subjectRepository.findSubjectById(id);
    }

    @Transactional
    @Override
    public Subject createSubject(Subject subject) {
        return subjectRepository.save(subject);
    }

    @Override
    public Subject findById(long id) {
        return subjectRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Subject not found with id:"+id));
    }

    @Override
    public long countByCollegeId(long collegeId) {
        return 0;
    }

    @Override
    public long countSubjectsByCollegeId(Long collegeId) {
        return 0;
    }

}

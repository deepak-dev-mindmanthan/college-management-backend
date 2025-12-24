package org.collegemanagement.services;

import org.collegemanagement.entity.academic.Subject;

import java.util.List;

public interface SubjectService {
    List<Subject> findByCourseId(long id);
    Subject createSubject(Subject subject);
    Subject findById(long id);
    long countByCollegeId(long collegeId);

    long countSubjectsByCollegeId(Long collegeId);
}

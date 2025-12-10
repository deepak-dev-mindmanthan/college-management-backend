package org.collegemanagement.services;

import org.collegemanagement.entity.Course;

import java.util.List;

public interface CourseService {

    Course createCourse(Course course);
    List<Course> findByCollegeId(Long collegeId);
    Course findById(Long courseId);
    boolean exitsByCourseId(Long courseId);

}

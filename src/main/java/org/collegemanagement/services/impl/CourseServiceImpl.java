package org.collegemanagement.services.impl;

import org.collegemanagement.entity.Course;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.repositories.CollegeRepository;
import org.collegemanagement.repositories.CourseRepository;
import org.collegemanagement.services.CourseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CollegeRepository collegeRepository;

    public CourseServiceImpl(CourseRepository courseRepository, CollegeRepository collegeRepository) {
        this.courseRepository = courseRepository;
        this.collegeRepository = collegeRepository;
    }

    @Transactional
    @Override
    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }

    @Override
    public List<Course> findByCollegeId(Long collegeId) {
        return courseRepository.findByCollegeId(collegeId);
    }

    @Override
    public Course findById(Long courseId) {
        Optional<Course> courseOptional = courseRepository.findById(courseId);
        if(courseOptional.isPresent()) {
            return courseOptional.get();
        }
        else{
            throw new ResourceNotFoundException("Course with id " + courseId + " not found");
        }
    }

    @Override
    public boolean exitsByCourseId(Long courseId) {
        return courseRepository.existsById(courseId);
    }

}

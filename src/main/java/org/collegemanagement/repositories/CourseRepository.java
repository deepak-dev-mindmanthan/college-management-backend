package org.collegemanagement.repositories;
import org.collegemanagement.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByCollegeId(Long collegeId);
}

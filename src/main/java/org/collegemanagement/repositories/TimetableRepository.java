package org.collegemanagement.repositories;

import org.collegemanagement.entity.timetable.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TimetableRepository extends JpaRepository<Timetable, Long> {

    @Query("""
            SELECT t FROM Timetable t
            WHERE t.teacher.id = :teacherId
            AND t.college.id = :collegeId
            ORDER BY t.dayOfWeek, t.periodNumber
            """)
    List<Timetable> findByTeacherIdAndCollegeId(@Param("teacherId") Long teacherId, @Param("collegeId") Long collegeId);
}


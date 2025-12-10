package org.collegemanagement.repositories;

import org.collegemanagement.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findBySubjectIdAndDate(Long subjectId, LocalDate date);
    List<Attendance> findByStudentId(Long studentId);
    @Query("SELECT (COUNT(a) FILTER (WHERE a.status = 'PRESENT') * 100.0) / COUNT(a) FROM Attendance a WHERE a.student.id = :studentId")
    double getAttendancePercentage(@Param("studentId") Long studentId);

}
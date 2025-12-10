package org.collegemanagement.services;

import org.collegemanagement.entity.Attendance;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    List<Attendance> findBySubjectIdAndDate(Long subjectId, LocalDate date);
    List<Attendance> findByStudentId(Long studentId);
    List<Attendance> findByStudentId(long studentId);
    Attendance save(Attendance attendance);
    double getAttendancePercentage(long studentId);
}

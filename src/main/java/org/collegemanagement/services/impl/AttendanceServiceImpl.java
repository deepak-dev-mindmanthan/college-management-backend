package org.collegemanagement.services.impl;

import org.collegemanagement.entity.Attendance;
import org.collegemanagement.repositories.AttendanceRepository;
import org.collegemanagement.services.AttendanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;


@Service
public class AttendanceServiceImpl implements AttendanceService {
    private final AttendanceRepository attendanceRepository;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    @Override
    public List<Attendance> findBySubjectIdAndDate(Long subjectId, LocalDate date) {
        return attendanceRepository.findBySubjectIdAndDate(subjectId, date);
    }

    @Override
    public List<Attendance> findByStudentId(Long studentId) {
        return attendanceRepository.findByStudentId(studentId);
    }

    @Override
    public List<Attendance> findByStudentId(long studentId) {
        return attendanceRepository.findByStudentId(studentId);
    }


    @Transactional
    @Override
    public Attendance save(Attendance attendance) {
        return attendanceRepository.save(attendance);
    }

    @Override
    public double getAttendancePercentage(long studentId) {
        return attendanceRepository.getAttendancePercentage(studentId);
    }
}

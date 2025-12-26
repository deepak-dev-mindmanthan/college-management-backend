package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.attendance.*;
import org.collegemanagement.entity.academic.ClassRoom;
import org.collegemanagement.entity.academic.StudentEnrollment;
import org.collegemanagement.entity.attendance.AttendanceRecord;
import org.collegemanagement.entity.attendance.AttendanceSession;
import org.collegemanagement.entity.student.Student;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.enums.AttendanceStatus;
import org.collegemanagement.exception.ResourceConflictException;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.mapper.AttendanceMapper;
import org.collegemanagement.repositories.AttendanceRecordRepository;
import org.collegemanagement.repositories.AttendanceSessionRepository;
import org.collegemanagement.repositories.ClassRoomRepository;
import org.collegemanagement.repositories.StudentEnrollmentRepository;
import org.collegemanagement.repositories.StudentRepository;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.AttendanceService;
import org.collegemanagement.services.CollegeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final ClassRoomRepository classRoomRepository;
    private final StudentRepository studentRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final TenantAccessGuard tenantAccessGuard;
    private final CollegeService collegeService;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public AttendanceSessionResponse createAttendanceSession(CreateAttendanceSessionRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find and validate class
        ClassRoom classRoom = classRoomRepository.findByUuidAndCollegeId(request.getClassUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with UUID: " + request.getClassUuid()));

        // Get college and validate tenant access
        College college = getCollegeById(collegeId);

        // Check if session already exists
        if (attendanceSessionRepository.findByClassIdAndDateAndSessionTypeAndCollegeId(
                classRoom.getId(), request.getDate(), request.getSessionType(), collegeId).isPresent()) {
            throw new ResourceConflictException(
                    "Attendance session already exists for class " + classRoom.getName() +
                    " on " + request.getDate() + " with session type " + request.getSessionType());
        }

        // Create attendance session
        AttendanceSession session = AttendanceSession.builder()
                .college(college)
                .classRoom(classRoom)
                .date(request.getDate())
                .sessionType(request.getSessionType())
                .records(new HashSet<>())
                .build();

        session = attendanceSessionRepository.save(session);

        return AttendanceMapper.toSessionResponse(session);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public AttendanceSessionResponse getSessionByUuid(String sessionUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        AttendanceSession session = attendanceSessionRepository.findByUuidAndCollegeId(sessionUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance session not found with UUID: " + sessionUuid));

        return AttendanceMapper.toSessionResponse(session);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public AttendanceSessionResponse markAttendance(MarkAttendanceRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find attendance session
        AttendanceSession session = attendanceSessionRepository.findByUuidAndCollegeId(request.getSessionUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance session not found with UUID: " + request.getSessionUuid()));

        // Get all enrolled students for the class (active enrollments)
        List<StudentEnrollment> enrollments = studentEnrollmentRepository.findByClassIdAndCollegeId(
                session.getClassRoom().getId(), collegeId);

        Set<AttendanceRecord> records = new HashSet<>();

        // Process each student in the request
        for (MarkAttendanceRequest.StudentAttendanceRecord record : request.getRecords()) {
            // Find student
            Student student = studentRepository.findByUuidAndCollegeId(record.getStudentUuid(), collegeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + record.getStudentUuid()));

            // Verify student is enrolled in the class
            boolean isEnrolled = enrollments.stream()
                    .anyMatch(enrollment -> enrollment.getStudent().getId().equals(student.getId()));
            if (!isEnrolled) {
                throw new ResourceConflictException(
                        "Student " + student.getUser().getName() + " is not enrolled in class " + session.getClassRoom().getName());
            }

            // Check if record already exists
            AttendanceRecord existingRecord = attendanceRecordRepository
                    .findBySessionIdAndStudentId(session.getId(), student.getId())
                    .orElse(null);

            if (existingRecord != null) {
                // Update existing record
                existingRecord.setStatus(record.getStatus());
                records.add(attendanceRecordRepository.save(existingRecord));
            } else {
                // Create new record
                AttendanceRecord attendanceRecord = AttendanceRecord.builder()
                        .attendanceSession(session)
                        .student(student)
                        .status(record.getStatus())
                        .build();
                records.add(attendanceRecordRepository.save(attendanceRecord));
            }
        }

        // Update session's records collection
        session.getRecords().clear();
        session.getRecords().addAll(records);
        session = attendanceSessionRepository.save(session);

        return AttendanceMapper.toSessionResponse(session);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public AttendanceRecordResponse updateAttendanceRecord(String recordUuid, UpdateAttendanceRecordRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find attendance record
        AttendanceRecord record = attendanceRecordRepository.findByUuidAndCollegeId(recordUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found with UUID: " + recordUuid));

        // Update status
        record.setStatus(request.getStatus());
        record = attendanceRecordRepository.save(record);

        return AttendanceMapper.toRecordResponse(record);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public AttendanceRecordResponse getRecordByUuid(String recordUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        AttendanceRecord record = attendanceRecordRepository.findByUuidAndCollegeId(recordUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found with UUID: " + recordUuid));

        return AttendanceMapper.toRecordResponse(record);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public List<AttendanceRecordResponse> getRecordsBySession(String sessionUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find session
        AttendanceSession session = attendanceSessionRepository.findByUuidAndCollegeId(sessionUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance session not found with UUID: " + sessionUuid));

        // Get all records for the session
        List<AttendanceRecord> records = attendanceRecordRepository.findBySessionIdAndCollegeId(session.getId(), collegeId);

        return AttendanceMapper.toRecordResponseList(records);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public Page<AttendanceSessionResponse> getSessionsByClass(String classUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate class exists
        classRoomRepository.findByUuidAndCollegeId(classUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with UUID: " + classUuid));

        Page<AttendanceSession> sessions = attendanceSessionRepository.findByClassUuidAndCollegeId(classUuid, collegeId, pageable);

        return sessions.map(AttendanceMapper::toSessionResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public List<AttendanceSessionResponse> getSessionsByClassAndDateRange(String classUuid, LocalDate startDate, LocalDate endDate) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate class exists
        classRoomRepository.findByUuidAndCollegeId(classUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with UUID: " + classUuid));

        // Validate date range
        if (startDate.isAfter(endDate)) {
            throw new ResourceConflictException("Start date cannot be after end date");
        }

        List<AttendanceSession> sessions = attendanceSessionRepository.findByClassUuidAndDateRangeAndCollegeId(
                classUuid, startDate, endDate, collegeId);

        return sessions.stream()
                .map(AttendanceMapper::toSessionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public Page<AttendanceRecordResponse> getRecordsByStudent(String studentUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate student exists
        studentRepository.findByUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + studentUuid));

        Page<AttendanceRecord> records = attendanceRecordRepository.findByStudentUuidAndCollegeId(studentUuid, collegeId, pageable);

        return records.map(AttendanceMapper::toRecordResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public List<AttendanceRecordResponse> getRecordsByStudentAndDateRange(String studentUuid, LocalDate startDate, LocalDate endDate) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate student exists
        studentRepository.findByUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + studentUuid));

        // Validate date range
        if (startDate.isAfter(endDate)) {
            throw new ResourceConflictException("Start date cannot be after end date");
        }

        List<AttendanceRecord> records = attendanceRecordRepository.findByStudentUuidAndDateRangeAndCollegeId(
                studentUuid, startDate, endDate, collegeId);

        return AttendanceMapper.toRecordResponseList(records);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public AttendanceSummaryResponse getStudentAttendanceSummary(String studentUuid, LocalDate startDate, LocalDate endDate) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate student exists
        Student student = studentRepository.findByUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + studentUuid));

        // Validate date range
        if (startDate.isAfter(endDate)) {
            throw new ResourceConflictException("Start date cannot be after end date");
        }

        // Get active enrollment for student
        StudentEnrollment enrollment = studentEnrollmentRepository.findActiveByStudentIdAndCollegeId(student.getId(), collegeId)
                .orElse(null);

        // Calculate statistics
        Long totalDays = attendanceRecordRepository.countByStudentIdAndDateRangeAndCollegeId(
                student.getId(), startDate, endDate, collegeId);
        Long presentDays = attendanceRecordRepository.countByStudentIdAndStatusAndDateRangeAndCollegeId(
                student.getId(), AttendanceStatus.PRESENT, startDate, endDate, collegeId);
        Long absentDays = attendanceRecordRepository.countByStudentIdAndStatusAndDateRangeAndCollegeId(
                student.getId(), AttendanceStatus.ABSENT, startDate, endDate, collegeId);
        Long lateDays = attendanceRecordRepository.countByStudentIdAndStatusAndDateRangeAndCollegeId(
                student.getId(), AttendanceStatus.LATE, startDate, endDate, collegeId);

        // Calculate percentage
        Double attendancePercentage = (totalDays != null && totalDays > 0) ?
                ((presentDays != null ? presentDays.doubleValue() : 0.0) / totalDays.doubleValue()) * 100.0 : 0.0;

        return AttendanceSummaryResponse.builder()
                .studentUuid(student.getUuid())
                .studentName(student.getUser() != null ? student.getUser().getName() : null)
                .rollNumber(student.getRollNumber())
                .classUuid(enrollment != null && enrollment.getClassRoom() != null ? enrollment.getClassRoom().getUuid() : null)
                .className(enrollment != null && enrollment.getClassRoom() != null ? enrollment.getClassRoom().getName() : null)
                .startDate(startDate)
                .endDate(endDate)
                .totalDays(totalDays != null ? totalDays : 0L)
                .presentDays(presentDays != null ? presentDays : 0L)
                .absentDays(absentDays != null ? absentDays : 0L)
                .lateDays(lateDays != null ? lateDays : 0L)
                .attendancePercentage(attendancePercentage)
                .build();
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ClassAttendanceSummaryResponse getClassAttendanceSummary(String classUuid, LocalDate startDate, LocalDate endDate) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate class exists
        ClassRoom classRoom = classRoomRepository.findByUuidAndCollegeId(classUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with UUID: " + classUuid));

        // Validate date range
        if (startDate.isAfter(endDate)) {
            throw new ResourceConflictException("Start date cannot be after end date");
        }

        // Get total students in class (active enrollments)
        List<StudentEnrollment> enrollments = studentEnrollmentRepository.findByClassIdAndCollegeId(
                classRoom.getId(), collegeId);
        Long totalStudents = (long) enrollments.size();

        // Count total sessions
        Long totalSessions = attendanceSessionRepository.countDistinctSessionsByClassIdAndDateRangeAndCollegeId(
                classRoom.getId(), startDate, endDate, collegeId);

        // Calculate statistics
        Long totalPresent = attendanceRecordRepository.countByClassIdAndStatusAndDateRangeAndCollegeId(
                classRoom.getId(), AttendanceStatus.PRESENT, startDate, endDate, collegeId);
        Long totalAbsent = attendanceRecordRepository.countByClassIdAndStatusAndDateRangeAndCollegeId(
                classRoom.getId(), AttendanceStatus.ABSENT, startDate, endDate, collegeId);
        Long totalLate = attendanceRecordRepository.countByClassIdAndStatusAndDateRangeAndCollegeId(
                classRoom.getId(), AttendanceStatus.LATE, startDate, endDate, collegeId);

        // Calculate average attendance percentage
        Long totalRecords = attendanceRecordRepository.countByClassIdAndDateRangeAndCollegeId(
                classRoom.getId(), startDate, endDate, collegeId);
        Double averageAttendancePercentage = (totalRecords != null && totalRecords > 0) ?
                ((totalPresent != null ? totalPresent.doubleValue() : 0.0) / totalRecords.doubleValue()) * 100.0 : 0.0;

        return ClassAttendanceSummaryResponse.builder()
                .classUuid(classRoom.getUuid())
                .className(classRoom.getName())
                .section(classRoom.getSection())
                .startDate(startDate)
                .endDate(endDate)
                .totalStudents(totalStudents)
                .totalSessions(totalSessions != null ? totalSessions : 0L)
                .totalPresent(totalPresent != null ? totalPresent : 0L)
                .totalAbsent(totalAbsent != null ? totalAbsent : 0L)
                .totalLate(totalLate != null ? totalLate : 0L)
                .averageAttendancePercentage(averageAttendancePercentage)
                .build();
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public Page<AttendanceSessionResponse> getSessionsByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate date range
        if (startDate.isAfter(endDate)) {
            throw new ResourceConflictException("Start date cannot be after end date");
        }

        Page<AttendanceSession> sessions = attendanceSessionRepository.findByDateRangeAndCollegeId(
                startDate, endDate, collegeId, pageable);

        return sessions.map(AttendanceMapper::toSessionResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public List<AttendanceSessionResponse> getSessionsByDate(LocalDate date) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        List<AttendanceSession> sessions = attendanceSessionRepository.findByDateAndCollegeId(date, collegeId);

        return sessions.stream()
                .map(AttendanceMapper::toSessionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public void deleteSession(String sessionUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        AttendanceSession session = attendanceSessionRepository.findByUuidAndCollegeId(sessionUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance session not found with UUID: " + sessionUuid));

        // Delete session (cascades to records)
        attendanceSessionRepository.delete(session);
    }

    // Helper methods

    private College getCollegeById(Long collegeId) {
        College college = collegeService.findById(collegeId);
        tenantAccessGuard.assertCurrentTenant(college);
        return college;
    }
}


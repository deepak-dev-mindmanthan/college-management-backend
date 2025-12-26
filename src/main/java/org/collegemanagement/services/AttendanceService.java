package org.collegemanagement.services;

import org.collegemanagement.dto.attendance.*;
import org.collegemanagement.enums.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    /**
     * Create a new attendance session
     */
    AttendanceSessionResponse createAttendanceSession(CreateAttendanceSessionRequest request);

    /**
     * Get attendance session by UUID
     */
    AttendanceSessionResponse getSessionByUuid(String sessionUuid);

    /**
     * Mark attendance for students in a session
     */
    AttendanceSessionResponse markAttendance(MarkAttendanceRequest request);

    /**
     * Update an attendance record
     */
    AttendanceRecordResponse updateAttendanceRecord(String recordUuid, UpdateAttendanceRecordRequest request);

    /**
     * Get attendance record by UUID
     */
    AttendanceRecordResponse getRecordByUuid(String recordUuid);

    /**
     * Get all attendance records for a session
     */
    List<AttendanceRecordResponse> getRecordsBySession(String sessionUuid);

    /**
     * Get all attendance sessions for a class with pagination
     */
    Page<AttendanceSessionResponse> getSessionsByClass(String classUuid, Pageable pageable);

    /**
     * Get all attendance sessions for a class within date range
     */
    List<AttendanceSessionResponse> getSessionsByClassAndDateRange(String classUuid, LocalDate startDate, LocalDate endDate);

    /**
     * Get all attendance records for a student with pagination
     */
    Page<AttendanceRecordResponse> getRecordsByStudent(String studentUuid, Pageable pageable);

    /**
     * Get all attendance records for a student within date range
     */
    List<AttendanceRecordResponse> getRecordsByStudentAndDateRange(String studentUuid, LocalDate startDate, LocalDate endDate);

    /**
     * Get attendance summary for a student within date range
     */
    AttendanceSummaryResponse getStudentAttendanceSummary(String studentUuid, LocalDate startDate, LocalDate endDate);

    /**
     * Get attendance summary for a class within date range
     */
    ClassAttendanceSummaryResponse getClassAttendanceSummary(String classUuid, LocalDate startDate, LocalDate endDate);

    /**
     * Get all attendance sessions within date range with pagination
     */
    Page<AttendanceSessionResponse> getSessionsByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Get all attendance sessions for a specific date
     */
    List<AttendanceSessionResponse> getSessionsByDate(LocalDate date);

    /**
     * Delete attendance session (cascades to records)
     */
    void deleteSession(String sessionUuid);
}


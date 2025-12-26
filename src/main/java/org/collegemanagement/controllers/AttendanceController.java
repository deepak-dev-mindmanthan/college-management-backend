package org.collegemanagement.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.collegemanagement.api.response.ApiResponse;
import org.collegemanagement.dto.attendance.*;
import org.collegemanagement.services.AttendanceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@AllArgsConstructor
@Tag(name = "Attendance Management", description = "APIs for managing student attendance in the college management system")
public class AttendanceController {

    private final AttendanceService attendanceService;

    // ========== Session Management Endpoints ==========

    @Operation(
            summary = "Create attendance session",
            description = "Creates a new attendance session for a class on a specific date. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Attendance session created successfully",
                    content = @Content(schema = @Schema(implementation = AttendanceSessionResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Attendance session already exists for this class, date, and session type"
            )
    })
    @PostMapping("/sessions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<AttendanceSessionResponse>> createSession(
            @Valid @RequestBody CreateAttendanceSessionRequest request
    ) {
        AttendanceSessionResponse session = attendanceService.createAttendanceSession(request);
        return ResponseEntity.ok(ApiResponse.success(session, "Attendance session created successfully"));
    }

    @Operation(
            summary = "Get attendance session by UUID",
            description = "Retrieves attendance session information by UUID. Accessible by all authenticated users."
    )
    @GetMapping("/sessions/{sessionUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<AttendanceSessionResponse>> getSession(
            @Parameter(description = "UUID of the attendance session")
            @PathVariable String sessionUuid
    ) {
        AttendanceSessionResponse session = attendanceService.getSessionByUuid(sessionUuid);
        return ResponseEntity.ok(ApiResponse.success(session, "Attendance session retrieved successfully"));
    }

    @Operation(
            summary = "Get all attendance sessions for a class",
            description = "Retrieves a paginated list of all attendance sessions for a specific class. Accessible by all authenticated users."
    )
    @GetMapping("/classes/{classUuid}/sessions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<Page<AttendanceSessionResponse>>> getSessionsByClass(
            @Parameter(description = "UUID of the class")
            @PathVariable String classUuid,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "date") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<AttendanceSessionResponse> sessions = attendanceService.getSessionsByClass(classUuid, pageable);
        return ResponseEntity.ok(ApiResponse.success(sessions, "Attendance sessions retrieved successfully"));
    }

    @Operation(
            summary = "Get attendance sessions for a class within date range",
            description = "Retrieves all attendance sessions for a class within a date range. Accessible by all authenticated users."
    )
    @GetMapping("/classes/{classUuid}/sessions/range")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<AttendanceSessionResponse>>> getSessionsByClassAndDateRange(
            @Parameter(description = "UUID of the class")
            @PathVariable String classUuid,
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<AttendanceSessionResponse> sessions = attendanceService.getSessionsByClassAndDateRange(classUuid, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(sessions, "Attendance sessions retrieved successfully"));
    }

    @Operation(
            summary = "Get attendance sessions by date range",
            description = "Retrieves a paginated list of all attendance sessions within a date range. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping("/sessions/range")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<AttendanceSessionResponse>>> getSessionsByDateRange(
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AttendanceSessionResponse> sessions = attendanceService.getSessionsByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(sessions, "Attendance sessions retrieved successfully"));
    }

    @Operation(
            summary = "Get attendance sessions by date",
            description = "Retrieves all attendance sessions for a specific date. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping("/sessions/date/{date}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<AttendanceSessionResponse>>> getSessionsByDate(
            @Parameter(description = "Date (yyyy-MM-dd)")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<AttendanceSessionResponse> sessions = attendanceService.getSessionsByDate(date);
        return ResponseEntity.ok(ApiResponse.success(sessions, "Attendance sessions retrieved successfully"));
    }

    @Operation(
            summary = "Delete attendance session",
            description = "Deletes an attendance session and all associated records. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @DeleteMapping("/sessions/{sessionUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSession(
            @Parameter(description = "UUID of the attendance session to delete")
            @PathVariable String sessionUuid
    ) {
        attendanceService.deleteSession(sessionUuid);
        return ResponseEntity.ok(ApiResponse.success(null, "Attendance session deleted successfully"));
    }

    // ========== Attendance Marking Endpoints ==========

    @Operation(
            summary = "Mark attendance",
            description = "Marks attendance for students in a session. Creates or updates attendance records. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @PostMapping("/mark")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<AttendanceSessionResponse>> markAttendance(
            @Valid @RequestBody MarkAttendanceRequest request
    ) {
        AttendanceSessionResponse session = attendanceService.markAttendance(request);
        return ResponseEntity.ok(ApiResponse.success(session, "Attendance marked successfully"));
    }

    // ========== Attendance Record Endpoints ==========

    @Operation(
            summary = "Get attendance record by UUID",
            description = "Retrieves attendance record information by UUID. Accessible by all authenticated users."
    )
    @GetMapping("/records/{recordUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> getRecord(
            @Parameter(description = "UUID of the attendance record")
            @PathVariable String recordUuid
    ) {
        AttendanceRecordResponse record = attendanceService.getRecordByUuid(recordUuid);
        return ResponseEntity.ok(ApiResponse.success(record, "Attendance record retrieved successfully"));
    }

    @Operation(
            summary = "Update attendance record",
            description = "Updates an attendance record status. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @PutMapping("/records/{recordUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> updateRecord(
            @Parameter(description = "UUID of the attendance record")
            @PathVariable String recordUuid,
            @Valid @RequestBody UpdateAttendanceRecordRequest request
    ) {
        AttendanceRecordResponse record = attendanceService.updateAttendanceRecord(recordUuid, request);
        return ResponseEntity.ok(ApiResponse.success(record, "Attendance record updated successfully"));
    }

    @Operation(
            summary = "Get all attendance records for a session",
            description = "Retrieves all attendance records for a specific session. Accessible by all authenticated users."
    )
    @GetMapping("/sessions/{sessionUuid}/records")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> getRecordsBySession(
            @Parameter(description = "UUID of the attendance session")
            @PathVariable String sessionUuid
    ) {
        List<AttendanceRecordResponse> records = attendanceService.getRecordsBySession(sessionUuid);
        return ResponseEntity.ok(ApiResponse.success(records, "Attendance records retrieved successfully"));
    }

    @Operation(
            summary = "Get all attendance records for a student",
            description = "Retrieves a paginated list of all attendance records for a specific student. Accessible by all authenticated users."
    )
    @GetMapping("/students/{studentUuid}/records")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<Page<AttendanceRecordResponse>>> getRecordsByStudent(
            @Parameter(description = "UUID of the student")
            @PathVariable String studentUuid,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "sessionDate") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<AttendanceRecordResponse> records = attendanceService.getRecordsByStudent(studentUuid, pageable);
        return ResponseEntity.ok(ApiResponse.success(records, "Attendance records retrieved successfully"));
    }

    @Operation(
            summary = "Get attendance records for a student within date range",
            description = "Retrieves all attendance records for a student within a date range. Accessible by all authenticated users."
    )
    @GetMapping("/students/{studentUuid}/records/range")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> getRecordsByStudentAndDateRange(
            @Parameter(description = "UUID of the student")
            @PathVariable String studentUuid,
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<AttendanceRecordResponse> records = attendanceService.getRecordsByStudentAndDateRange(studentUuid, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(records, "Attendance records retrieved successfully"));
    }

    // ========== Summary Endpoints ==========

    @Operation(
            summary = "Get student attendance summary",
            description = "Retrieves attendance summary statistics for a student within a date range. Accessible by all authenticated users."
    )
    @GetMapping("/students/{studentUuid}/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<AttendanceSummaryResponse>> getStudentAttendanceSummary(
            @Parameter(description = "UUID of the student")
            @PathVariable String studentUuid,
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        AttendanceSummaryResponse summary = attendanceService.getStudentAttendanceSummary(studentUuid, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(summary, "Attendance summary retrieved successfully"));
    }

    @Operation(
            summary = "Get class attendance summary",
            description = "Retrieves attendance summary statistics for a class within a date range. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping("/classes/{classUuid}/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<ClassAttendanceSummaryResponse>> getClassAttendanceSummary(
            @Parameter(description = "UUID of the class")
            @PathVariable String classUuid,
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        ClassAttendanceSummaryResponse summary = attendanceService.getClassAttendanceSummary(classUuid, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(summary, "Class attendance summary retrieved successfully"));
    }
}


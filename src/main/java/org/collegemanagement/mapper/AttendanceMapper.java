package org.collegemanagement.mapper;

import org.collegemanagement.dto.attendance.AttendanceRecordResponse;
import org.collegemanagement.dto.attendance.AttendanceSessionResponse;
import org.collegemanagement.entity.attendance.AttendanceRecord;
import org.collegemanagement.entity.attendance.AttendanceSession;
import org.collegemanagement.enums.AttendanceStatus;

import java.util.List;
import java.util.stream.Collectors;

public final class AttendanceMapper {

    private AttendanceMapper() {
    }

    /**
     * Convert AttendanceSession entity to AttendanceSessionResponse
     */
    public static AttendanceSessionResponse toSessionResponse(AttendanceSession session) {
        if (session == null) {
            return null;
        }

        // Calculate statistics from records
        List<AttendanceRecord> records = session.getRecords() != null ? 
            session.getRecords().stream().toList() : List.of();
        
        long totalStudents = records.size();
        long presentCount = records.stream()
            .filter(r -> r.getStatus() == AttendanceStatus.PRESENT)
            .count();
        long absentCount = records.stream()
            .filter(r -> r.getStatus() == AttendanceStatus.ABSENT)
            .count();
        long lateCount = records.stream()
            .filter(r -> r.getStatus() == AttendanceStatus.LATE)
            .count();

        return AttendanceSessionResponse.builder()
                .uuid(session.getUuid())
                .classUuid(session.getClassRoom() != null ? session.getClassRoom().getUuid() : null)
                .className(session.getClassRoom() != null ? session.getClassRoom().getName() : null)
                .section(session.getClassRoom() != null ? session.getClassRoom().getSection() : null)
                .date(session.getDate())
                .sessionType(session.getSessionType())
                .totalStudents(totalStudents)
                .presentCount(presentCount)
                .absentCount(absentCount)
                .lateCount(lateCount)
                .collegeId(session.getCollege() != null ? session.getCollege().getId() : null)
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }

    /**
     * Convert AttendanceRecord entity to AttendanceRecordResponse
     */
    public static AttendanceRecordResponse toRecordResponse(AttendanceRecord record) {
        if (record == null) {
            return null;
        }

        AttendanceSession session = record.getAttendanceSession();
        var student = record.getStudent();

        return AttendanceRecordResponse.builder()
                .uuid(record.getUuid())
                .sessionUuid(session != null ? session.getUuid() : null)
                .sessionDate(session != null ? session.getDate() : null)
                .sessionType(session != null ? session.getSessionType() != null ? session.getSessionType().name() : null : null)
                .studentUuid(student != null ? student.getUuid() : null)
                .studentName(student != null && student.getUser() != null ? student.getUser().getName() : null)
                .rollNumber(student != null ? student.getRollNumber() : null)
                .classUuid(session != null && session.getClassRoom() != null ? session.getClassRoom().getUuid() : null)
                .className(session != null && session.getClassRoom() != null ? session.getClassRoom().getName() : null)
                .status(record.getStatus())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }

    /**
     * Convert list of AttendanceRecord entities to list of AttendanceRecordResponse
     */
    public static List<AttendanceRecordResponse> toRecordResponseList(List<AttendanceRecord> records) {
        if (records == null) {
            return List.of();
        }
        return records.stream()
                .map(AttendanceMapper::toRecordResponse)
                .collect(Collectors.toList());
    }
}


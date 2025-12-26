package org.collegemanagement.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.AttendanceSessionType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSessionResponse {

    private String uuid;
    private String classUuid;
    private String className;
    private String section;
    private LocalDate date;
    private AttendanceSessionType sessionType;
    private Long totalStudents;
    private Long presentCount;
    private Long absentCount;
    private Long lateCount;
    private Long collegeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


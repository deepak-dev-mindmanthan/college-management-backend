package org.collegemanagement.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecordResponse {

    private String uuid;
    private String sessionUuid;
    private LocalDate sessionDate;
    private String sessionType;
    private String studentUuid;
    private String studentName;
    private String rollNumber;
    private String classUuid;
    private String className;
    private AttendanceStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


package org.collegemanagement.dto.attendance;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.AttendanceStatus;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkAttendanceRequest {

    @NotNull(message = "Session UUID is required")
    private String sessionUuid;

    @NotEmpty(message = "At least one attendance record is required")
    private List<StudentAttendanceRecord> records;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentAttendanceRecord {
        @NotNull(message = "Student UUID is required")
        private String studentUuid;

        @NotNull(message = "Attendance status is required")
        private AttendanceStatus status;
    }
}


package org.collegemanagement.dto.attendance;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.AttendanceStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAttendanceRecordRequest {

    @NotNull(message = "Attendance status is required")
    private AttendanceStatus status;
}


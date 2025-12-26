package org.collegemanagement.dto.attendance;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.AttendanceSessionType;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAttendanceSessionRequest {

    @NotNull(message = "Class UUID is required")
    private String classUuid;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Session type is required")
    private AttendanceSessionType sessionType;
}


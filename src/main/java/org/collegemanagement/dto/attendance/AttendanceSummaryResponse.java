package org.collegemanagement.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSummaryResponse {

    private String studentUuid;
    private String studentName;
    private String rollNumber;
    private String classUuid;
    private String className;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalDays;
    private Long presentDays;
    private Long absentDays;
    private Long lateDays;
    private Double attendancePercentage;
}


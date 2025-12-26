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
public class ClassAttendanceSummaryResponse {

    private String classUuid;
    private String className;
    private String section;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalStudents;
    private Long totalSessions;
    private Long totalPresent;
    private Long totalAbsent;
    private Long totalLate;
    private Double averageAttendancePercentage;
}


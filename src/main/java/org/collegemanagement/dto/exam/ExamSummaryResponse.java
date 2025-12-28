package org.collegemanagement.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.ExamType;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSummaryResponse {

    private String examUuid;
    private String examName;
    private ExamType examType;
    private String academicYearName;
    private Instant startDate;
    private Instant endDate;
    private Integer totalClasses;
    private Integer totalSubjects;
    private Integer totalStudents;
    private Integer studentsWithMarks;
    private Boolean isCompleted;
}


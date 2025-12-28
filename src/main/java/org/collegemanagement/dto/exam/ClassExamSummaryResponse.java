package org.collegemanagement.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassExamSummaryResponse {

    private String examUuid;
    private String examName;
    private String classUuid;
    private String className;
    private String section;
    private Integer totalStudents;
    private Integer studentsWithMarks;
    private Integer totalSubjects;
    private BigDecimal averagePercentage;
    private Integer passedStudents;
    private Integer failedStudents;
    private BigDecimal passPercentage;
}


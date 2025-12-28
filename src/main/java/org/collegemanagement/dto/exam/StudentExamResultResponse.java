package org.collegemanagement.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentExamResultResponse {

    private String examUuid;
    private String examName;
    private String studentUuid;
    private String studentName;
    private String rollNumber;
    private String className;
    private String section;
    private List<StudentMarksResponse> marks;
    private Integer totalMarks;
    private Integer obtainedMarks;
    private BigDecimal percentage;
    private String overallGrade;
    private BigDecimal overallGradePoints;
    private Boolean isPassed;
    private Integer rankInClass;
}


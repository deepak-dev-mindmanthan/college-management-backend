package org.collegemanagement.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentMarksResponse {

    private String uuid;
    private String examSubjectUuid;
    private String examSubjectName;
    private String studentUuid;
    private String studentName;
    private String rollNumber;
    private Integer marksObtained;
    private Integer maxMarks;
    private Integer passMarks;
    private String grade;
    private BigDecimal gradePoints;
    private Boolean isPassed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


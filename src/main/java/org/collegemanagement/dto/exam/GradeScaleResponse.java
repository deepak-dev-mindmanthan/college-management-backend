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
public class GradeScaleResponse {

    private String uuid;
    private String grade;
    private Integer minMarks;
    private Integer maxMarks;
    private BigDecimal gradePoints;
    private Long collegeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


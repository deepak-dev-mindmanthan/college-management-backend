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
public class UpdateGradeScaleRequest {

    private String grade;
    private Integer minMarks;
    private Integer maxMarks;
    private BigDecimal gradePoints;
}


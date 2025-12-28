package org.collegemanagement.dto.exam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGradeScaleRequest {

    @NotBlank(message = "Grade is required")
    private String grade;

    @NotNull(message = "Minimum marks is required")
    private Integer minMarks;

    @NotNull(message = "Maximum marks is required")
    private Integer maxMarks;

    @NotNull(message = "Grade points is required")
    private BigDecimal gradePoints;
}


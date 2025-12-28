package org.collegemanagement.dto.exam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStudentMarksRequest {

    @NotBlank(message = "Exam subject UUID is required")
    private String examSubjectUuid;

    @NotBlank(message = "Student UUID is required")
    private String studentUuid;

    @NotNull(message = "Marks obtained is required")
    private Integer marksObtained;
}


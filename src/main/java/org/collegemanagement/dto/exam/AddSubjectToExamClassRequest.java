package org.collegemanagement.dto.exam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddSubjectToExamClassRequest {

    @NotBlank(message = "Subject UUID is required")
    private String subjectUuid;

    @NotNull(message = "Maximum marks is required")
    private Integer maxMarks;

    @NotNull(message = "Pass marks is required")
    private Integer passMarks;

    @NotNull(message = "Exam date is required")
    private LocalDate examDate;

    /**
     * Optional: UUID of the teacher assigned to evaluate this exam subject
     */
    private String assignedTeacherUuid;
}


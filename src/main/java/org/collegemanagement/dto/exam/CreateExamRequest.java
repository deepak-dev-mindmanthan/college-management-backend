package org.collegemanagement.dto.exam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.ExamType;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateExamRequest {

    @NotBlank(message = "Exam name is required")
    private String name;

    @NotNull(message = "Exam type is required")
    private ExamType examType;

    @NotBlank(message = "Academic year UUID is required")
    private String academicYearUuid;

    private Instant startDate;

    private Instant endDate;

    private Set<String> classUuids; // Classes participating in this exam
}


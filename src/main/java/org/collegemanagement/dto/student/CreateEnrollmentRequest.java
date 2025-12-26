package org.collegemanagement.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.EnrollmentStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEnrollmentRequest {

    @NotBlank(message = "Academic year UUID is required")
    private String academicYearUuid;

    @NotBlank(message = "Class UUID is required")
    private String classUuid;

    private String rollNumber;

    @NotNull(message = "Status is required")
    private EnrollmentStatus status;
}


package org.collegemanagement.dto.promotion;

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
public class PromoteStudentRequest {

    @NotBlank(message = "Student UUID is required")
    private String studentUuid;

    @NotBlank(message = "Academic year UUID is required")
    private String academicYearUuid;

    @NotBlank(message = "To class UUID is required")
    private String toClassUuid;

    private String rollNumber; // Optional roll number for new enrollment

    @NotNull(message = "Enrollment status is required")
    private EnrollmentStatus enrollmentStatus; // Status for new enrollment (usually ACTIVE)

    private String remarks; // Optional remarks about the promotion
}


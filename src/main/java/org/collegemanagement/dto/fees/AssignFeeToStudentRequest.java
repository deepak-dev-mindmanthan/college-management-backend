package org.collegemanagement.dto.fees;

import jakarta.validation.constraints.NotEmpty;
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
public class AssignFeeToStudentRequest {

    @NotNull(message = "Student UUID is required")
    private String studentUuid;

    @NotNull(message = "Fee structure UUID is required")
    private String feeStructureUuid;

    private LocalDate dueDate;
}


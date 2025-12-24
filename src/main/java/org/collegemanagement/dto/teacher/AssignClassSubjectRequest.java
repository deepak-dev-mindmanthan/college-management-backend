package org.collegemanagement.dto.teacher;

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
public class AssignClassSubjectRequest {

    @NotBlank(message = "Class UUID is required")
    private String classUuid;

    @NotBlank(message = "Subject UUID is required")
    private String subjectUuid;
}


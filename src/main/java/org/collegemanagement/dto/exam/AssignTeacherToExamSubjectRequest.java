package org.collegemanagement.dto.exam;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignTeacherToExamSubjectRequest {

    @NotBlank(message = "Teacher UUID is required")
    private String teacherUuid;
}


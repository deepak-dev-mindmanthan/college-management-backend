package org.collegemanagement.dto.exam;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStudentMarksRequest {

    @NotNull(message = "Marks obtained is required")
    private Integer marksObtained;
}


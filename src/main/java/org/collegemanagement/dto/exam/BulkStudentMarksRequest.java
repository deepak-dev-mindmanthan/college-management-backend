package org.collegemanagement.dto.exam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkStudentMarksRequest {

    @NotBlank(message = "Exam subject UUID is required")
    private String examSubjectUuid;

    @NotNull(message = "Marks list is required")
    private List<StudentMarksEntry> marks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentMarksEntry {
        @NotBlank(message = "Student UUID is required")
        private String studentUuid;

        @NotNull(message = "Marks obtained is required")
        private Integer marksObtained;
    }
}


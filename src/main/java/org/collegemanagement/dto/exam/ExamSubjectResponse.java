package org.collegemanagement.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSubjectResponse {

    private String uuid;
    private String examClassUuid;
    private String subjectUuid;
    private String subjectName;
    private String subjectCode;
    private Integer maxMarks;
    private Integer passMarks;
    private LocalDate examDate;
    private Integer totalStudents;
    private Integer studentsWithMarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


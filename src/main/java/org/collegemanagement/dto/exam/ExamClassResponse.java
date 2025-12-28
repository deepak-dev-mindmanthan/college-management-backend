package org.collegemanagement.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamClassResponse {

    private String uuid;
    private String examUuid;
    private String classUuid;
    private String className;
    private String section;
    private Set<ExamSubjectResponse> subjects;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


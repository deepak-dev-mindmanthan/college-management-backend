package org.collegemanagement.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassSubjectInfo {
    private String classUuid;
    private String className;
    private String section;
    private String subjectUuid;
    private String subjectName;
    private String subjectCode;
}


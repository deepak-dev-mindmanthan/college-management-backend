package org.collegemanagement.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.EnrollmentStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentInfo {
    private String uuid;
    private String academicYearUuid;
    private String academicYearName;
    private String classUuid;
    private String className;
    private String section;
    private String rollNumber;
    private EnrollmentStatus status;
    private LocalDateTime createdAt;
}


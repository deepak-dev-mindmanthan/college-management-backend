package org.collegemanagement.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.ExamType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamResponse {

    private String uuid;
    private String name;
    private ExamType examType;
    private String academicYearUuid;
    private String academicYearName;
    private Instant startDate;
    private Instant endDate;
    private Long collegeId;
    private Set<ExamClassResponse> examClasses;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


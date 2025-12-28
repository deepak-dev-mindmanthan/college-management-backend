package org.collegemanagement.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.ResultStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentTranscriptResponse {

    private String uuid;
    private String studentUuid;
    private String studentName;
    private String rollNumber;
    private String academicYearUuid;
    private String academicYearName;
    private BigDecimal cgpa;
    private Integer totalCredits;
    private ResultStatus resultStatus;
    private Boolean published;
    private String approvedByUuid;
    private String approvedByName;
    private Instant publishedAt;
    private String remarks;
    private List<StudentMarksResponse> marks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


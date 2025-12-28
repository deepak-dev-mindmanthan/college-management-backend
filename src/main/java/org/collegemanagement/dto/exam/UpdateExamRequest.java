package org.collegemanagement.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.ExamType;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateExamRequest {

    private String name;

    private ExamType examType;

    private String academicYearUuid;

    private Instant startDate;

    private Instant endDate;

    private Set<String> classUuids; // Classes to add/remove from exam
}


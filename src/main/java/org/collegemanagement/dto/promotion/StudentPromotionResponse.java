package org.collegemanagement.dto.promotion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentPromotionResponse {

    private String uuid;
    private String studentUuid;
    private String studentName;
    private String fromClassUuid;
    private String fromClassName;
    private String toClassUuid;
    private String toClassName;
    private String academicYearUuid;
    private String academicYearName;
    private String promotedByUserUuid;
    private String promotedByName;
    private String remarks;
    private Long collegeId;
}


package org.collegemanagement.dto.ptm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PTMBookingResponse {

    private String uuid;
    private String slotUuid;
    private LocalDate slotDate;
    private LocalTime slotStartTime;
    private LocalTime slotEndTime;
    private String teacherUuid;
    private String teacherName;
    private String parentUuid;
    private String parentName;
    private String studentUuid;
    private String studentName;
    private Instant bookedAt;
    private String remarks;
    private Long collegeId;
}


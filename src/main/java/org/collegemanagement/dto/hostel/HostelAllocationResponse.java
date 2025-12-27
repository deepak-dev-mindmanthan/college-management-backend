package org.collegemanagement.dto.hostel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HostelAllocationResponse {

    private String uuid;
    private String studentUuid;
    private String studentName;
    private String rollNumber;
    private String roomUuid;
    private String roomNumber;
    private String hostelUuid;
    private String hostelName;
    private Instant allocatedAt;
    private Instant releasedAt;
    private Boolean isActive; // true if releasedAt is null
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


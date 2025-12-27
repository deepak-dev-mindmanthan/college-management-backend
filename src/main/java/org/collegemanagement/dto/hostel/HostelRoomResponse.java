package org.collegemanagement.dto.hostel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HostelRoomResponse {

    private String uuid;
    private String hostelUuid;
    private String hostelName;
    private String roomNumber;
    private Integer capacity;
    private Integer currentOccupancy; // Calculated from active allocations
    private Integer availableSpots; // capacity - currentOccupancy
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


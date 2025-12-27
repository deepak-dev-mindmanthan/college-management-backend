package org.collegemanagement.dto.hostel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HostelSummaryResponse {

    private long totalHostels;
    private long totalRooms;
    private long totalActiveAllocations;
    private long totalInactiveAllocations;
    private long totalStudents;
    private long totalStudentsWithHostel;
    private long totalStudentsWithoutHostel;
    private long totalCapacity;
    private long totalOccupied;
    private long totalAvailable;
}


package org.collegemanagement.dto.transport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportSummaryResponse {

    private Long totalRoutes;
    private Long totalActiveAllocations;
    private Long totalInactiveAllocations;
    private Long totalStudentsWithTransport;
    private Long totalStudentsWithoutTransport;
    private Long totalStudents;
}


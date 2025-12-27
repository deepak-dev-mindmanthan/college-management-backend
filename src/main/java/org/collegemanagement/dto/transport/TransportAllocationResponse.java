package org.collegemanagement.dto.transport;

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
public class TransportAllocationResponse {

    private String uuid;
    private String studentUuid;
    private String studentName;
    private String rollNumber;
    private String routeUuid;
    private String routeName;
    private String vehicleNo;
    private String driverName;
    private String pickupPoint;
    private Instant allocatedAt;
    private Instant releasedAt;
    private Boolean isActive; // true if releasedAt is null
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


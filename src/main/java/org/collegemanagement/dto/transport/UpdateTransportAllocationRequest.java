package org.collegemanagement.dto.transport;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTransportAllocationRequest {

    private String routeUuid;

    @Size(max = 200, message = "Pickup point must not exceed 200 characters")
    private String pickupPoint;

    private Instant releasedAt; // Set to release/deactivate allocation
}

